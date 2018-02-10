/*
 * Copyright 25/1/18 4:43 PM randhirgupta
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

package com.randhir.musicplayer.presenter.main

import android.app.Activity
import android.content.Context
import com.randhir.musicplayer.MusicPlayerApp
import com.randhir.musicplayer.component.DaggerPresenterComponent
import com.randhir.musicplayer.network.ApiModule
import com.randhir.musicplayer.network.RxObservableConverter
import com.randhir.musicplayer.network.api.MusicPlayerApis
import com.randhir.musicplayer.network.response.Song
import com.randhir.musicplayer.preference.StorageUtil
import com.randhir.musicplayer.presenter.BasePresenter
import com.randhir.musicplayer.presenter.NetworkPresenterView
import javax.inject.Inject


/**
 * @author randhirgupta
 *  @since 25/1/18.
 */
class MainPresenter constructor(context: Activity) : BasePresenter<MainPresenter.View>() {

    override var view: View? = context as View

    @Inject
    lateinit var api: MusicPlayerApis

    init {
        DaggerPresenterComponent.builder()
                .appComponent(MusicPlayerApp.getAppComponent(context))
                .apiModule(ApiModule())
                .build().inject(this)
    }

    fun getAllSongs() {
        view?.run {
            this.showProgress()
            addToDisposable(RxObservableConverter.forNetwork(api.getAllSongs())
                    .subscribe({
                        this.hideProgress()
                        this.getAllSongList(it)
                    }, {
                        this.hideProgress()
                    }))
        }
    }

    fun playAudio(context: Context, audioIndex: Int, audioList: ArrayList<Song>, serviceBound: Boolean) {

        //Check is service is active
        if (!serviceBound) {
            //Store Serializable audioList to SharedPreferences
            val storage = StorageUtil(context)
            storage.storeAudio(audioList)
            storage.storeAudioIndex(audioIndex)
            view?.startBindService()
        } else {
            //Store the new audioIndex to SharedPreferences
            val storage = StorageUtil(context)
            storage.storeAudioIndex(audioIndex)
            view?.sendBroadcastIntent()
        }
    }

    interface View : NetworkPresenterView {
        fun getAllSongList(songList: ArrayList<Song>)
        fun startBindService()
        fun sendBroadcastIntent()
        fun showProgress()
        fun hideProgress()
    }
}