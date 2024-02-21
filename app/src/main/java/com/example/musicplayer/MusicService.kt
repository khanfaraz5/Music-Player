package com.example.musicplayer

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat

//MusicService is a background service responsible for managing music playback.
//Extends the Android Service class, allowing it to run in the background.


// for running application in the background
class MusicService: Service(){

    private var myBinder = MyBinder()
    var mediaPlayer:MediaPlayer?=null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable

    override fun onBind(intent: Intent?): IBinder? {
        mediaSession = MediaSessionCompat(baseContext,"My Music")
        return myBinder
    }

    inner class MyBinder: Binder(){
        fun currentService(): MusicService{
            return this@MusicService
        }
    }
    fun showNotification(playPauseBtn: Int){
// so that clicking on notification brigs us back to MainActivity even if app is closed
        val intent = Intent(baseContext, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0,intent, PendingIntent.FLAG_IMMUTABLE)


        val prevIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

// These PendingIntent objects are used to handle the actions when the user interacts with the notification.

        val playIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val exitIntent = Intent(baseContext, NotificationReciever::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.mipmap.music_player_icon)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.music_player_splash_screen))
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true) // for only one notification for every song
            .addAction(R.drawable.previous_icon,"Previous", prevPendingIntent)
            .addAction(playPauseBtn,"Play", playPendingIntent)
            .addAction(R.drawable.next_icon,"Next", nextPendingIntent)
            .addAction(R.drawable.exit_icon,"Exit", exitPendingIntent)
            .build()

        startForeground(13, notification)

    // It's set to be in the foreground using startForeground, ensuring the service continues running,
    // and the notification remains visible even if the app is in the background.

    }

  fun createMediaPlayer(){
        try {
            if (PlayerActivity.musicService!!.mediaPlayer == null) {
                PlayerActivity.musicService!!. mediaPlayer = MediaPlayer()
            }

            // !! means that mediaPlayer is not null and can be played
            PlayerActivity.musicService!!.mediaPlayer!!.reset()
            // playing song according to the user click
            PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
            PlayerActivity.musicService!!.mediaPlayer!!.prepare()
            PlayerActivity.binding.playPauseBtnPA.setImageResource(R.drawable.pause_icon)
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon) // because music play has started now , so pause button will be showed in notification
            // when the user changes songs , the change should also be displayed on notification bar
            // This line of code is specially for notification bar.

            // for applying seekbar feature on notification
            PlayerActivity.binding.tvSeekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.tvSeekBarEnd.text = formatDuration(mediaPlayer!!.duration.toLong())
            PlayerActivity.binding.seekBarPA.progress = 0 // initial progress is 0
            PlayerActivity.binding.seekBarPA.max = mediaPlayer!!.duration // after completion of song , progress is 100
            PlayerActivity.nowPlayingID = PlayerActivity.musicListPA[PlayerActivity.songPosition].id
        }
        catch (e: Exception){
            return
        }


 // now this will increment the progress of seekbar when the song is playing
    }
    fun seekbarSetup(){
        runnable = Runnable {
            PlayerActivity.binding.tvSeekBarStart.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seekBarPA.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }
}