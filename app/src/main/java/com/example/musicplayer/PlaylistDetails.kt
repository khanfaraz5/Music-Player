package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.ActivityPlaylistDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

class PlaylistDetails : AppCompatActivity() {
    lateinit var binding:ActivityPlaylistDetailsBinding
    lateinit var adapter: MusicAdapter
    companion object{
        var currentPlaylistPosition: Int = -1
    }

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.color.cool_pink)
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentPlaylistPosition = intent.extras?.get("index") as Int
        binding.playlistDetailsRV.setItemViewCacheSize(10)
        binding.playlistDetailsRV.setHasFixedSize(true)
        binding.playlistDetailsRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this,PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist, playlistDetails = true)
        binding.playlistDetailsRV.adapter = adapter  // now initializing the adapter

        binding.backBtnPLD.setOnClickListener {
            finish()
        }

        binding.shuffleBtnPD.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)// for going to
            // PlayerActivity when clicked on shuffle button
            intent.putExtra("index", 0)
            intent.putExtra("class", "PlaylistDetailsShuffle")
            startActivity(intent)
        }

        binding.addBtnPD.setOnClickListener {
            startActivity(Intent(this,SelectionActivity::class.java))
        }

        binding.removeBtnPD.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this, R.color.cool_pink)
            builder.setTitle("Remove")
                .setMessage("Do you want to remove all songs from this playlist ?")
                .setPositiveButton("yes"){dialog,_ ->
                    PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist.clear()
                    adapter.refreshPlaylist()
                    dialog.dismiss()
                }
                .setNegativeButton("No"){dialog,_ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.RED)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED)
        }
    }

    // setting name and details for playlist
    override fun onResume() {
        super.onResume()
        binding.playlistNamePD.text = PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].name
        binding.moreInfoPD.text = "Total ${adapter.itemCount} Songs.\n\n" +
                "Created On:\n${PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].createdOn}\n\n" +
                "-- ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].createdBy}"
        // if playlist has songs count > 0 then First song's image will be displayed on top of playlist
        if(adapter.itemCount > 0){
            Glide.with(this)
                .load(PlaylistActivity.musicPlaylist.ref[currentPlaylistPosition].playlist[0].artURI)
                .apply(RequestOptions().placeholder(R.mipmap.music_player_icon).centerCrop())
                .into(binding.playlistImgPD)

            binding.shuffleBtnPD.visibility = View.VISIBLE
        }
        adapter.notifyDataSetChanged()
        val editor = getSharedPreferences("Favorites", MODE_PRIVATE).edit() // MODE_PRIVATE so that no other apps can access the data
        // for storing playlist data using shared preferences (since songs disappeared from favorites section after closing app)
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist",jsonStringPlaylist)
        editor.apply()

    }


}