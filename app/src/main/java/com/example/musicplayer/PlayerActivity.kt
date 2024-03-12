package com.example.musicplayer

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener { // ServiceConnection will help us connect
    // with the service we created
    companion object{
        lateinit var musicListPA : ArrayList<Music>
        var songPosition: Int=0

        var isPlaying: Boolean = false
        var musicService: MusicService?=null
        lateinit var binding:ActivityPlayerBinding
        var repeat: Boolean = false
        var isFavorite: Boolean = false // if current song is favorite or not
        var fIndex: Int = -1
        var nowPlayingID: String = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Base_Theme_MusicPlayer)

        binding  = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        InitializeLayout()

        // so that clicking on back button closes the current activity
        binding.backBtnPA.setOnClickListener {
            finish()
        }

        binding.playPauseBtnPA.setOnClickListener {

            // for changing icons , functions will be called based on if song is getting played or not

            if(isPlaying) pauseMusic()
            else playMusic()
        }

        binding.previousBtnPA.setOnClickListener {
            prevNextSong(increment = false)
        }

        binding.nextBtnPA.setOnClickListener {
            prevNextSong(increment = true)
        }

        binding.seekBarPA.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
               if(fromUser) musicService!!.mediaPlayer!!.seekTo(progress)  // checking is user has touched the seekbar and changed the current
                // progress of song
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?)= Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?)= Unit
        })

        // this is for repeat button
        binding.repeatBtnPA.setOnClickListener {
            if (!repeat){
                repeat = true
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.cool_green)) // this is for color change when repeat button
                // clicked
            }
            else{
                repeat = false
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.cool_pink)) // resetting the color of repeat button
            }
        }
    }



    // for displaying proper song name and image in playerActivity screen
    private fun setLayout(){
//        fIndex = favoriteChecker(musicListPA[songPosition].id) // checking if a song is favorite or not by song's id
//        Glide.with(this)
//            .load(musicListPA[songPosition].artURI)
//            .apply(RequestOptions().placeholder(R.mipmap.music_player_icon).centerCrop())
//            .into(binding.songImgPA)
//        binding.songNamePA.text = musicListPA[songPosition].title
//
//        if(repeat){
//            binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this,R.color.cool_green))
//        }
//        if(isFavorite) binding.favoriteBtnPA.setImageResource(R.drawable.favorite_icon)
//        else binding.favoriteBtnPA.setImageResource(R.drawable.favorite_empty)
        Glide.with(this)
            .load(musicListPA[songPosition].artURI)
            .apply(RequestOptions().placeholder(R.mipmap.music_player_icon).centerCrop())
            .into(binding.songImgPA)
        binding.songNamePA.text = musicListPA[songPosition].title

        if (repeat) {
            binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_green))
        }

    }

    private fun createMediaPlayer(){
        try {
            if (musicService!!.mediaPlayer == null) {
                musicService!!. mediaPlayer = MediaPlayer()
            }

            // !! means that mediaPlayer is not null and can be played
            musicService!!.mediaPlayer!!.reset()
            // playing song according to the user click
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.playPauseBtnPA.setImageResource(R.drawable.pause_icon)
            musicService!!.showNotification(R.drawable.pause_icon ) // because music play has started now , so pause button will be showed in notification
            // when the user changes songs , the change should also be displayed on notification bar
           // This line of code is specially for notification bar.

            // now defining song start and end duration for seekbar
            binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0 // initial progress is 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration // after completion of song , progress is 100

            // jaise hi hamara musicService ka object create hga , waise hi hamara setOnCompletionListener call hojaega
            musicService!!.mediaPlayer!!.setOnCompletionListener (this)

            // so that song should not start again if it is clicked again while playing
            nowPlayingID = musicListPA[songPosition].id
        }
        catch (e: Exception){
            return
        }

        binding.equalizerBtnPA.setOnClickListener {
            val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            eqIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, musicService!!.mediaPlayer!!.audioSessionId)
            eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
            eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE , AudioEffect.CONTENT_TYPE_MUSIC)
            try {
                startActivityForResult(eqIntent, 13)
            }catch (e: Exception){
                Toast.makeText(this,"Equalizer Feature Not Supported", Toast.LENGTH_SHORT).show()
            }
        }

        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*" // "audio" for audio , "video" for video and so on
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path)) // we have to pass it in Uri otherwise there wil be errors
            startActivity(Intent.createChooser(shareIntent,"Sharing Music File !!")) // to choose sharing method i.e whatsapp , insta etc
        }
        // if song is already added then do not add it again
//        binding.favoriteBtnPA.setOnClickListener {
//            if(isFavorite){
//                isFavorite = false
//                binding.favoriteBtnPA.setImageResource(R.drawable.favorite_empty)
//                FavoriteActivity.favoriteSongs.removeAt(fIndex)
//            }
//            else{
//                isFavorite = true
//                binding.favoriteBtnPA.setImageResource(R.drawable.favorite_icon)
//                FavoriteActivity.favoriteSongs.add(musicListPA[songPosition])
//            }
//        }

    }

    private fun InitializeLayout(){
        // catching passed intent from mainActivity , this code is for playing the song by identifying the song from its position
        // and playing it if it is not null
        // intents are passed from both MusicAdapter and MainActivity ,so we will operate based as per condition
        songPosition = intent.getIntExtra("index", 0)
        when(intent.getStringExtra("class")){

            "NowPlaying"->{
                // opening playerActivity when fragment is clicked ,
                // everything should be as it is such as song progress , seekbar timer etc
                setLayout()
                binding.tvSeekBarStart.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekBarEnd.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
                if(isPlaying) binding.playPauseBtnPA.setImageResource(R.drawable.pause_icon)
                else binding.playPauseBtnPA.setImageResource(R.drawable.play_icon_new)
            }
            "MusicAdapter" ->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()

            }
            "MainActivity"->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()

            }
//            "favoriteAdapter"->   {
//                val intent = Intent(this, MusicService::class.java)
//                bindService(intent, this, BIND_AUTO_CREATE)
//                startService(intent)
//                musicListPA = ArrayList()
//                musicListPA.addAll(FavoriteActivity.favoriteSongs)
//                setLayout()
//            }

//            "FavoriteShuffle"->{
//                val intent = Intent(this, MusicService::class.java)
//                bindService(intent, this, BIND_AUTO_CREATE)
//                startService(intent)
//                musicListPA = ArrayList()
//                musicListPA.addAll(FavoriteActivity.favoriteSongs)
//                musicListPA.shuffle()
//                setLayout()
//            }
            "PlaylistDetailsAdapter" ->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPosition].playlist)
                setLayout()
            }
            "PlaylistDetailsShuffle" ->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPosition].playlist)
                musicListPA.shuffle()
                setLayout()
            }
        }
    }

    private fun playMusic() {
        binding.playPauseBtnPA.setImageResource(R.drawable.pause_icon)
        musicService!!.showNotification(R.drawable.pause_icon)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()

    }
    private fun pauseMusic(){
        binding.playPauseBtnPA.setImageResource(R.drawable.play_icon_new)
        musicService!!.showNotification(R.drawable.play_icon_new)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    // for playing previous or next song based on button clicked , everytime a new song is traversed , a new layout and MediaPlayer will be created
    private fun prevNextSong(increment:Boolean){
        if(increment)
        {
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        }
        else{
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekbarSetup()

    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }
// when song completes , new song should be played automatically
    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setLayout() // for changing the image of song album whenever new song is automatically played
        } catch (e: Exception){
            return
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==13 || resultCode == RESULT_OK){
            return
        }
    }

}