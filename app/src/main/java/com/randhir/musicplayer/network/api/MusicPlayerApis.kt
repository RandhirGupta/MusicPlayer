/*
 * Copyright 25/1/18 4:18 PM randhirgupta
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

package com.randhir.musicplayer.network.api

import com.randhir.musicplayer.network.response.Song
import io.reactivex.Observable
import retrofit2.http.GET

/**
 * @author randhirgupta
 *  @since 25/1/18.
 */
interface MusicPlayerApis {

    @GET("/studio")
    fun getAllSongs(): Observable<ArrayList<Song>>
}