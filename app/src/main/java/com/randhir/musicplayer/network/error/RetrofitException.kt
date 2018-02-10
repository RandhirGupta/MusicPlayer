/*
 * Copyright 25/1/18 4:22 PM randhirgupta
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

import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * @author randhirgupta
 *  @since 25/1/18.
 */
class RetrofitException(override val message: String?, val response: Response<*>?,
                        val kind: ErrorKind, val exception: Throwable?, val retrofit: Retrofit?) : RuntimeException(message, exception) {

    fun isTimeout(): Boolean {
        return exception is SocketTimeoutException
    }

    fun <T> getErrorBodyAs(type: Class<T>): T? {
        if (retrofit == null || response == null || response.errorBody() == null) {
            return null
        }

        val converter: Converter<ResponseBody, T> = retrofit.responseBodyConverter(type, arrayOf())
        return converter.convert(response.errorBody())
    }

    companion object {
        fun httpError(response: Response<*>, retrofit: Retrofit?): RetrofitException {
            val message: String = response.code().toString() + " " + response.message()
            return RetrofitException(message, response, ErrorKind.HTTP, null, retrofit)
        }

        fun networkError(exception: IOException): RetrofitException {
            return RetrofitException(exception.message, null, ErrorKind.NETWORK, exception, null)
        }

        fun unexpectedError(exception: Throwable): RetrofitException {
            return RetrofitException(exception.message, null, ErrorKind.UNEXPECTED, exception, null)
        }
    }
}