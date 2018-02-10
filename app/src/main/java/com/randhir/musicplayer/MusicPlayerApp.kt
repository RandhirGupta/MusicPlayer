/*
 * Copyright 25/1/18 5:07 PM randhirgupta
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

package com.randhir.musicplayer

import android.app.Application
import android.content.Context
import com.randhir.musicplayer.component.AppComponent
import com.randhir.musicplayer.component.DaggerAppComponent
import com.randhir.musicplayer.network.NetworkModule

/**
 * @author randhirgupta
 *  @since 25/1/18.
 */
class MusicPlayerApp : Application() {

    val singleton: AppComponent by lazy {
        DaggerAppComponent.builder()
                .networkModule(NetworkModule())
                .appModule(AppModule(this))
                .build()
    }

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        fun getAppComponent(context: Context?): AppComponent {
            return (context?.applicationContext as MusicPlayerApp).singleton
        }
    }
}