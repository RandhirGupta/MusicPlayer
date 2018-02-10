/*
 * Copyright 25/1/18 4:53 PM randhirgupta
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

package com.randhir.musicplayer.ui.activity.main

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.Button
import com.randhir.musicplayer.ActivityModule
import com.randhir.musicplayer.MusicPlayerApp
import com.randhir.musicplayer.R
import com.randhir.musicplayer.component.DaggerActivityComponent
import com.randhir.musicplayer.network.error.ErrorResponse
import com.randhir.musicplayer.network.response.Song
import com.randhir.musicplayer.presenter.ActivityPresenterModule
import com.randhir.musicplayer.presenter.BasePresenter
import com.randhir.musicplayer.presenter.main.MainPresenter
import com.randhir.musicplayer.service.ExoPlayerService
import com.randhir.musicplayer.ui.activity.PresenterActivity
import com.randhir.musicplayer.util.ProgressDialogUtil
import javax.inject.Inject


class MainActivity : PresenterActivity<MainPresenter.View>(), MainPresenter.View, View.OnClickListener {

    private val Broadcast_PLAY_NEW_AUDIO = "com.randhir.musicplayer.service.Broadcast_PLAY_NEW_AUDIO"
    val REQUEST_ID_MULTIPLE_PERMISSIONS = 1

    @Inject
    lateinit var mPresenter: MainPresenter

    private var mExoPlayerService: ExoPlayerService? = null
    private var mAudioList: ArrayList<Song> = ArrayList()
    private var serviceBound = false

    //Binding this Client to the AudioPlayer Service
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as ExoPlayerService.LocalBinder
            mExoPlayerService = binder.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
        }
    }

    override fun getPresenter(): BasePresenter<MainPresenter.View>? {
        return mPresenter
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_main
    }

    override fun inject() {
        DaggerActivityComponent.builder()
                .appComponent(MusicPlayerApp.getAppComponent(this))
                .activityModule(ActivityModule(this))
                .activityPresenterModule(ActivityPresenterModule())
                .build().inject(this)
    }

    override fun handleNetworkError(error: ErrorResponse) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        findViewById<Button>(R.id.play_pause_button).setOnClickListener(this)

        mPresenter.getAllSongs()
    }

    override fun getAllSongList(songList: ArrayList<Song>) {
        if (!songList.isEmpty()) {
            mAudioList.addAll(songList)
        }
    }

    override fun startBindService() {
        val playerIntent = Intent(this, ExoPlayerService::class.java)
        startService(playerIntent)
        bindService(playerIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun sendBroadcastIntent() {
        //Service is active
        //Send a broadcast to the service -> PLAY_NEW_AUDIO
        val broadcastIntent = Intent(Broadcast_PLAY_NEW_AUDIO)
        sendBroadcast(broadcastIntent)
    }

    override fun showProgress() {
        ProgressDialogUtil().showProgressDialog(this, getString(R.string.progress_message))
    }

    override fun hideProgress() {
        ProgressDialogUtil().dismissDialog()
    }

    override fun onClick(p0: View?) {

        when (p0?.id) {
            R.id.play_pause_button -> {
                if (checkAndRequestPermissions()) {
                    mPresenter.playAudio(this, 1, mAudioList, serviceBound)
                }
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {

        if (SDK_INT >= Build.VERSION_CODES.M) {
            val permissionReadPhoneState = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            val permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            val listPermissionsNeeded: ArrayList<String> = ArrayList()

            if (permissionReadPhoneState != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE)
            }

            if (permissionStorage != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
                return false
            } else {
                return true
            }
        }
        return false
    }
}
