package com.example.musicplayer

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.FragmentNowPlayingBinding


class NowPlaying : Fragment() {
    companion object{
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view) // initializing our binding
        binding.root.visibility = View.INVISIBLE /// if you started the app , and no song is playing , then no need for fragment to show up
        binding.playPauseBtnNP.setOnClickListener {
            if(PlayerActivity.isPlaying) pauseMusic() else playMusic()
        }
        binding.nextBtnNP.setOnClickListener {
            setSongPosition(increment =true)
            PlayerActivity.musicService!!.createMediaPlayer()
            // for displaying next/prev song name and image
            Glide.with(this)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artURI)
                .apply(RequestOptions().placeholder(R.mipmap.music_player_icon).centerCrop())
                .into(binding.songImgNP)
            // displaying the song name in textview
            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
            playMusic()

            binding.root.setOnClickListener {
                val intent = Intent(requireContext(), PlayerActivity::class.java)
                intent.putExtra("index", PlayerActivity.songPosition)
                intent.putExtra("class", "NowPlaying")
                ContextCompat.startActivity(requireContext(), intent,null)
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        // if music is playing , then make fragment visible
        if (PlayerActivity.musicService!=null){
            binding.root.visibility = View.VISIBLE
            binding.songNameNP.isSelected = true // for moving text
            //loading the image of song
            Glide.with(this)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artURI)
                .apply(RequestOptions().placeholder(R.mipmap.music_player_icon).centerCrop())
                .into(binding.songImgNP)
            // displaying the song name in textview
            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title

            if(PlayerActivity.isPlaying){
                binding.playPauseBtnNP.setImageResource(R.drawable.pause_icon)
            }
            else {
                binding.playPauseBtnNP.setImageResource(R.drawable.play_icon_new)
            }
        }
    }
    private fun playMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnNP.setImageResource(R.drawable.pause_icon)
        // so that icons of PlayerActivity and Notification also change
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_icon)
        PlayerActivity.binding.nextBtnPA.setImageResource(R.drawable.pause_icon)
        PlayerActivity.isPlaying = true
    }

    private fun pauseMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnNP.setImageResource(R.drawable.play_icon_new)
        // so that icons of PlayerActivity and Notification also change
        PlayerActivity.musicService!!.showNotification(R.drawable.play_icon_new)
        PlayerActivity.binding.nextBtnPA.setImageResource(R.drawable.play_icon_new)
        PlayerActivity.isPlaying = false
    }
}