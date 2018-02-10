/*
 * Copyright 2/2/18 7:32 PM randhirgupta
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

package com.randhir.musicplayer.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaSessionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.RemoteException
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import com.randhir.musicplayer.R
import com.randhir.musicplayer.network.response.Song
import com.randhir.musicplayer.preference.StorageUtil
import com.randhir.musicplayer.util.enum.PlaybackStatus


/**
 * @author randhirgupta
 *  @since 2/2/18.
 */
class ExoPlayerService : Service(), MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener, AudioManager.OnAudioFocusChangeListener {

    val Broadcast_PLAY_NEW_AUDIO = "com.randhir.musicplayer.service.Broadcast_PLAY_NEW_AUDIO"

    val ACTION_PLAY = "com.randhir.musicplayer.service.ACTION_PLAY"
    val ACTION_PAUSE = "com.randhir.musicplayer.service.ACTION_PAUSE"
    val ACTION_PREVIOUS = "com.randhir.musicplayer.service.ACTION_PREVIOUS"
    val ACTION_NEXT = "com.randhir.musicplayer.service.ACTION_NEXT"
    val ACTION_STOP = "com.randhir.musicplayer.service.ACTION_STOP"

    private var mMediaPlayer: MediaPlayer? = null

    //MediaSession
    var mMediaSessionManager: MediaSessionManager? = null
    var mMediaSession: MediaSessionCompat? = null
    var mTransportControls: MediaControllerCompat.TransportControls? = null

    //media player notification ID
    private val NOTIFICATION_ID = 101

    //Used to pause/resume Media player
    private var mResumePosition: Int = 0

    //Audio focus
    private var mAudioManager: AudioManager? = null

    //Binder given to clients
    private val mIBinder = LocalBinder()


    private var mAudioList: ArrayList<Song>? = null
    private var mAudioIndex = -1
    private var mActiveSong: Song? = null

    private var mIsCancelNotification: Boolean = true

    override fun onCreate() {
        super.onCreate()

        //ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        //Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
    }

    /**
     * Service Lifecycle methods
     */
    override fun onBind(p0: Intent?): IBinder {
        return mIBinder
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {

            //Load data from SharedPreferences
            val storage = StorageUtil(applicationContext)
            mAudioList = storage.loadAudio()
            mAudioIndex = storage.loadAudioIndex()

            if (mAudioIndex != -1 && mAudioIndex < mAudioList!!.size) {
                //index is in a valid range
                mActiveSong = mAudioList!![mAudioIndex]
            } else {
                stopSelf()
            }
        } catch (e: NullPointerException) {
            stopSelf()
        }


        //Request audio focus
        if (!requestAudioFocus()) {
            //Could not gain focus
            stopSelf()
        }

        if (mMediaSessionManager == null) {
            try {
                initMediaSession()
                initMediaPlayer()
            } catch (e: RemoteException) {
                e.printStackTrace()
                stopSelf()
            }

            buildNotification(PlaybackStatus.PLAYING, mIsCancelNotification)
        }

        //Handle Intent action from MediaSession.TransportControls
        handleIncomingActions(intent)
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mMediaSession?.release()
        removeNotification();
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        if (mMediaPlayer != null) {
            stopMedia()
            mMediaPlayer?.release()
        }
        removeAudioFocus()

        removeNotification();

        //unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReceiver);
        unregisterReceiver(playNewAudio);

        //clear cached playlist
        StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
    }

    override fun onPrepared(p0: MediaPlayer?) {
        //Invoked when the media source is ready for playback.
        playMedia()
    }

    override fun onError(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        when (p1) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> {
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + p2)
            }
            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> {
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + p2)
            }
            MediaPlayer.MEDIA_ERROR_UNKNOWN -> {
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKNOWN " + p2);
            }
        }
        return false
    }

    override fun onSeekComplete(p0: MediaPlayer?) {
    }

    override fun onInfo(p0: MediaPlayer?, p1: Int, p2: Int): Boolean {
        //Invoked to communicate some info
        return false
    }

    /**
     * MediaPlayer callback methods
     */
    override fun onBufferingUpdate(p0: MediaPlayer?, p1: Int) {

    }

    override fun onCompletion(p0: MediaPlayer?) {
        //Invoked when playback of a media source has completed.
        stopMedia()

        removeNotification()
        //stop the service
        stopSelf()
    }

    override fun onAudioFocusChange(p0: Int) {
        //Invoked when the audio focus of the system is updated.
        when (p0) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback
                if (mMediaPlayer == null)
                    initMediaPlayer()
                else if (!mMediaPlayer?.isPlaying()!!) mMediaPlayer?.start()
                mMediaPlayer?.setVolume(1.0f, 1.0f)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (mMediaPlayer?.isPlaying()!!) mMediaPlayer?.stop()
                mMediaPlayer?.release()
                mMediaPlayer = null
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->
                // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (mMediaPlayer?.isPlaying()!!) mMediaPlayer?.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->
                // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (mMediaPlayer?.isPlaying()!!) mMediaPlayer?.setVolume(0.1f, 0.1f)
        }
    }

    /**
     * AudioFocus
     */
    private fun requestAudioFocus(): Boolean {
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = mAudioManager?.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            //Focus gained
            return true
        }
        //Could not gain focus
        return false
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager?.abandonAudioFocus(this)
    }

    private fun initMediaPlayer() {

        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer()
        }

        //Set up MediaPlayer event listeners
        mMediaPlayer?.setOnCompletionListener(this)
        mMediaPlayer?.setOnErrorListener(this)
        mMediaPlayer?.setOnPreparedListener(this)
        mMediaPlayer?.setOnBufferingUpdateListener(this)
        mMediaPlayer?.setOnSeekCompleteListener(this)
        mMediaPlayer?.setOnInfoListener(this)

        //Reset so that the MediaPlayer is not to another data source
        mMediaPlayer?.reset()

        mMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mMediaPlayer?.setDataSource(mActiveSong?.url)
        mMediaPlayer?.prepareAsync()
    }

    private fun playMedia() {
        if (!mMediaPlayer?.isPlaying!!) {
            mIsCancelNotification = true
            mMediaPlayer?.start()
        }
    }

    private fun stopMedia() {
        if (mMediaPlayer == null) return
        if (mMediaPlayer?.isPlaying!!) {
            mIsCancelNotification = false
            mMediaPlayer?.stop()
        }
    }

    private fun pauseMedia() {
        if (mMediaPlayer?.isPlaying!!) {
            mIsCancelNotification = false
            mMediaPlayer?.pause()
            mResumePosition = mMediaPlayer?.currentPosition!!
        }
    }

    private fun resumeMedia() {
        if (!mMediaPlayer?.isPlaying!!) {
            mIsCancelNotification = true
            mMediaPlayer?.seekTo(mResumePosition)
            mMediaPlayer?.start()
        }
    }

    private fun skipToNext() {

        if (mAudioIndex == mAudioList?.size!! - 1) {
            //if last in playlist
            mAudioIndex = 0
            mActiveSong = mAudioList?.get(mAudioIndex)
        } else {
            //get next in playlist
            mActiveSong = mAudioList?.get(++mAudioIndex)
        }

        //Update stored index
        StorageUtil(getApplicationContext()).storeAudioIndex(mAudioIndex);

        stopMedia()
        //reset MediaPlayer
        mMediaPlayer?.reset()
        initMediaPlayer()
    }

    private fun skipToPrevious() {

        if (mAudioIndex == 0) {
            //if first in playlist
            //set index to the last of mAudioList
            mAudioIndex = mAudioList?.size!! - 1
            mActiveSong = mAudioList?.get(mAudioIndex)
        } else {
            //get previous in playlist
            mActiveSong = mAudioList?.get(--mAudioIndex)
        }

        //Update stored index
        StorageUtil(getApplicationContext()).storeAudioIndex(mAudioIndex)
        stopMedia()
        //reset MediaPlayer
        mMediaPlayer?.reset()
        initMediaPlayer()
    }

    /**
     * Service Binder
     */
    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): ExoPlayerService {
            return ExoPlayerService()
        }
    }

    /**
     * ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs
     */
    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            pauseMedia()
            buildNotification(PlaybackStatus.PAUSED, mIsCancelNotification)
        }
    }

    private fun registerBecomingNoisyReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, intentFilter)
    }

    /**
     * MediaSession and Notification actions
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Throws(RemoteException::class)
    private fun initMediaSession() {
        if (mMediaSessionManager != null) return  //mMediaSessionManager exists

        mMediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        // Create a new MediaSession
        mMediaSession = MediaSessionCompat(applicationContext, "AudioPlayer")
        //Get MediaSessions transport controls
        mTransportControls = mMediaSession?.getController()?.getTransportControls()
        //set MediaSession -> ready to receive media commands
        mMediaSession?.setActive(true)
        //indicate that the MediaSession handles transport control commands
        // through its MediaSessionCompat.Callback.
        mMediaSession?.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        //Set mMediaSession's MetaData
        updateMetaData()

        // Attach Callback to receive MediaSession updates
        mMediaSession?.setCallback(object : MediaSessionCompat.Callback() {
            // Implement callbacks
            override fun onPlay() {
                super.onPlay()

                resumeMedia()
                buildNotification(PlaybackStatus.PLAYING, mIsCancelNotification)
            }

            override fun onPause() {
                super.onPause()

                pauseMedia()
                buildNotification(PlaybackStatus.PAUSED, mIsCancelNotification)
            }

            override fun onSkipToNext() {
                super.onSkipToNext()

                skipToNext()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING, mIsCancelNotification)
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()

                skipToPrevious()
                updateMetaData()
                buildNotification(PlaybackStatus.PLAYING, mIsCancelNotification)
            }

            override fun onStop() {
                super.onStop()
                removeNotification()
                //Stop the service
                stopSelf()
            }

            override fun onSeekTo(position: Long) {
                super.onSeekTo(position)
            }
        })
    }

    private fun updateMetaData() {
        val albumArt = BitmapFactory.decodeResource(resources,
                R.drawable.ic_music_player) //replace with medias albumArt
        // Update the current metadata
        mMediaSession?.setMetadata(MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mActiveSong?.artists)
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mActiveSong?.artists)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mActiveSong?.song)
                .build())
    }

    private fun buildNotification(playBackStatus: PlaybackStatus, isCancel: Boolean) {


        /**
         * Notification actions -> playbackAction()
         *  0 -> Play
         *  1 -> Pause
         *  2 -> Next track
         *  3 -> Previous track
         */

        var notificationAction = android.R.drawable.ic_media_pause//needs to be initialized
        var play_pauseAction: PendingIntent? = null

        //Build a new notification according to the current state of the MediaPlayer
        if (playBackStatus === PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause
            //create the pause action
            play_pauseAction = playbackAction(1)
        } else if (playBackStatus === PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play
            //create the play action
            play_pauseAction = playbackAction(0)
        }

        val largeIcon = BitmapFactory.decodeResource(resources,
                R.drawable.ic_music_player) //replace with your own image

        // Create a new Notification
        val notificationBuilder = NotificationCompat.Builder(this)
                // Hide the timestamp
                .setShowWhen(false)
                // Set the Notification style
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        // Attach our MediaSession token
                        .setMediaSession(mMediaSession?.getSessionToken())
                        // Show our playback controls in the compat view
                        .setShowActionsInCompactView(0, 1, 2))
                // Set the Notification color
                .setColor(resources.getColor(R.color.colorBlack))
                // Set the large and small icons
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                // Set Notification content information
                .setContentText(mActiveSong?.artists)
                .setContentTitle(mActiveSong?.song)
                .setContentInfo(mActiveSong?.song)
                .setOngoing(isCancel)
                // Add playback actions
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2)) as NotificationCompat.Builder
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun playbackAction(actionNumber: Int): PendingIntent? {
        val playbackAction = Intent(this, ExoPlayerService::class.java)
        when (actionNumber) {
            0 -> {
                // Play
                playbackAction.action = ACTION_PLAY
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            1 -> {
                // Pause
                playbackAction.action = ACTION_PAUSE
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            2 -> {
                // Next track
                playbackAction.action = ACTION_NEXT
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
            3 -> {
                // Previous track
                playbackAction.action = ACTION_PREVIOUS
                return PendingIntent.getService(this, actionNumber, playbackAction, 0)
            }
        }
        return null
    }

    private fun removeNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun handleIncomingActions(playbackAction: Intent?) {
        if (playbackAction == null || playbackAction.action == null) return

        val actionString = playbackAction.action
        if (actionString!!.equals(ACTION_PLAY, ignoreCase = true)) {
            mTransportControls?.play()
        } else if (actionString.equals(ACTION_PAUSE, ignoreCase = true)) {
            mTransportControls?.pause()
        } else if (actionString.equals(ACTION_NEXT, ignoreCase = true)) {
            mTransportControls?.skipToNext()
        } else if (actionString.equals(ACTION_PREVIOUS, ignoreCase = true)) {
            mTransportControls?.skipToPrevious()
        } else if (actionString.equals(ACTION_STOP, ignoreCase = true)) {
            mTransportControls?.stop()
        }
    }


    /**
     * Play new Audio
     */
    private val playNewAudio = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            //Get the new media index form SharedPreferences
            mAudioIndex = StorageUtil(applicationContext).loadAudioIndex()
            if (mAudioIndex != -1 && mAudioIndex < mAudioList?.size!!) {
                //index is in a valid range
                mActiveSong = mAudioList?.get(mAudioIndex)
            } else {
                stopSelf()
            }

            //A PLAY_NEW_AUDIO action received
            //reset mediaPlayer to play the new Audio
            stopMedia()
            mMediaPlayer?.reset()
            initMediaPlayer()
            updateMetaData()
            buildNotification(PlaybackStatus.PLAYING, mIsCancelNotification)
        }
    }

    private fun register_playNewAudio() {
        //Register playNewMedia receiver
        val filter = IntentFilter(Broadcast_PLAY_NEW_AUDIO)
        registerReceiver(playNewAudio, filter)
    }

}
