package com.example.musicplayer

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicplayer.databinding.ActivityFavoriteBinding

class FavoriteActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoriteBinding
    private lateinit var adapter: favoriteAdapter

    companion object{
        var favoriteSongs: ArrayList<Music> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Base_Theme_MusicPlayer)
        binding  = ActivityFavoriteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var tempList = ArrayList<String>()

        // so that clicking on back button closes the current activity
        binding.backBtnFA.setOnClickListener {
            finish() }
        binding.favoriteRV.setHasFixedSize(true)
        binding.favoriteRV.setItemViewCacheSize(13)
        binding.favoriteRV.layoutManager = GridLayoutManager(this,4) // 4 items in one row
        adapter = favoriteAdapter(this , favoriteSongs )
        binding.favoriteRV.adapter = adapter

        if(favoriteSongs.size < 1) binding.shuffleBtnFA.visibility = View.INVISIBLE // shuffle button should not show up if
        // Favorites doesn't contain any song

        binding.shuffleBtnFA.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)// for going to
            // PlayerActivity when clicked on shuffle button
            intent.putExtra("index", 0)
            intent.putExtra("class", "FavoriteShuffle")
            startActivity(intent)
        }

    }
}