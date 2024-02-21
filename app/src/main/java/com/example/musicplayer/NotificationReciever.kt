package com.example.musicplayer

// for interacting with notification
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlin.system.exitProcess

class NotificationReciever: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PREVIOUS -> prevNextSong(increment = false, context = context!!) // value of increment will be false if going to previous song
            ApplicationClass.PLAY -> if(PlayerActivity.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT ->  prevNextSong(increment = true, context = context!!) // value of increment will be true if going to next song
            ApplicationClass.EXIT -> {
                PlayerActivity.musicService!!.stopForeground(true)
                PlayerActivity.musicService!!.mediaPlayer!!.release()
                PlayerActivity.musicService = null
                exitProcess(1)
            }

        }
    }

    // so that if we pause or play in our notification bar , our player Activity also gets paused or played i.e icon change in both
    private fun playMusic(){

        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
        PlayerActivity.binding.playPauseBtnPA.setImageResource(R.drawable.pause_icon)
        // if music played in fragment , then notification should also show the same
        NowPlaying.binding.playPauseBtnNP.setImageResource(R.drawable.pause_icon)

    }

    private fun pauseMusic(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon)
        PlayerActivity.binding.playPauseBtnPA.setImageResource(R.drawable.play_icon)
        // if music paused in fragment , then notification should also show the same
        NowPlaying.binding.playPauseBtnNP.setImageResource(R.drawable.play_icon_new)
    }
    // code for next and previous button functionality
    private fun prevNextSong(increment: Boolean , context: Context){
        setSongPosition(increment = increment)
       PlayerActivity.musicService!!.createMediaPlayer()
        // for displaying next/prev song name and image
        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artURI)
            .apply(RequestOptions().placeholder(R.mipmap.music_player_icon).centerCrop())
            .into(PlayerActivity.binding.songImgPA)
        PlayerActivity.binding.songNamePA.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title

        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artURI)
            .apply(RequestOptions().placeholder(R.mipmap.music_player_icon).centerCrop())
            .into(NowPlaying.binding.songImgNP)
        // displaying the song name in textview
        NowPlaying.binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        playMusic()
        // when we changed songs through notification after making a song favorite , then a problem occurred where favorite icon was filled
        // for the next song too , so this code rectifies that error
        PlayerActivity.fIndex = favoriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if(PlayerActivity.isFavorite){
            PlayerActivity.binding.favoriteBtnPA.setImageResource(R.drawable.favorite_icon)
        }
        else{
            PlayerActivity.binding.favoriteBtnPA.setImageResource(R.drawable.favorite_empty)
        }
    }
}