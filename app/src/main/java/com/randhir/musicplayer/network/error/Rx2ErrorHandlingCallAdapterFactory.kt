/*
 * Copyright 25/1/18 4:29 PM randhirgupta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.randhir.musicplayer.network.error

import io.reactivex.Observable
import io.reactivex.functions.Function
import retrofit2.*
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.IOException
import java.lang.reflect.Type

/**
 * @author randhirgupta
 *  @since 25/1/18.
 */
class Rx2ErrorHandlingCallAdapterFactory : CallAdapter.Factory() {
    private val original: RxJava2CallAdapterFactory = RxJava2CallAdapterFactory.create()

    @Suppress("UNCHECKED_CAST")
    override fun get(returnType: Type?, annotations: Array<out Annotation>?, retrofit: Retrofit): CallAdapter<Observable<Any>, Any>? {
        return RxCallAdapterWrapper(retrofit, original.get(returnType, annotations, retrofit) as CallAdapter<Observable<Any>, Any>)
    }

    companion object {
        fun create(): CallAdapter.Factory {
            return Rx2ErrorHandlingCallAdapterFactory()
        }

        class RxCallAdapterWrapper(val retrofit: Retrofit?, val wrapped: CallAdapter<Observable<Any>, Any>?) : CallAdapter<Observable<Any>, Any> {
            override fun adapt(call: Call<Observable<Any>>?): Any {
                return (wrapped?.adapt(call) as Observable<*>).onErrorResumeNext(Function { throwable ->
                    Observable.error(asRetrofitException(throwable))
                })
            }

            override fun responseType(): Type? {
                return wrapped?.responseType()
            }

            fun asRetrofitException(throwable: Throwable): RetrofitException {
                when (throwable) {
                    is HttpException -> {
                        val response: Response<*> = throwable.response()
                        return RetrofitException.httpError(response, retrofit)
                    }

                    is IOException -> return RetrofitException.networkError(throwable)
                    else -> return RetrofitException.unexpectedError(throwable)
                }
            }
        }
    }
}