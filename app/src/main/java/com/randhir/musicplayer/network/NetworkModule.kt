/*
 * Copyright 25/1/18 4:33 PM randhirgupta
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

package com.randhir.musicplayer.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.randhir.musicplayer.BuildConfig
import com.randhir.musicplayer.MusicPlayerApp
import com.randhir.musicplayer.network.error.Rx2ErrorHandlingCallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * @author randhirgupta
 *  @since 25/1/18.
 */
@Module
class NetworkModule {

    private val CONNECT_TIMEOUT: Long = 30
    private val WRITE_TIMEOUT: Long = 30
    private val READ_TIMEOUT: Long = 30
    private val baseUrl: String = "http://starlord.hackerearth.com"

    @Provides
    @Singleton
    fun provideCache(application: MusicPlayerApp): Cache {
        val cacheSize = 10 * 1024 * 1024 // 10MB
        return Cache(application.cacheDir, cacheSize.toLong())
    }

    @Provides
    @Singleton
    fun provideGs(): Gson {
        return GsonBuilder().create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(cache: Cache, interceptor: Interceptor): OkHttpClient {
        val logger = HttpLoggingInterceptor()
        if (BuildConfig.DEBUG) {
            logger.level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
                .cache(cache)
                .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
                .addInterceptor(logger)
                .addInterceptor(interceptor)
                .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(gs: Gson, client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
                .addCallAdapterFactory(Rx2ErrorHandlingCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gs))
                .baseUrl(baseUrl)
                .client(client)
                .build()
    }

    @Provides
    @Singleton
    fun provideInterceptor(): Interceptor {
        return Interceptor {
            val builder: Request.Builder = it.request().newBuilder()
            builder.header("User-Agent", "Android")

            it.proceed(builder.build())
        }
    }
}