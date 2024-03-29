package com.example.musicplayer

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.musicplayer.databinding.ActivityPlaylistBinding
import com.example.musicplayer.databinding.AddPlaylistDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding:ActivityPlaylistBinding
    private lateinit var adapter: PlaylistViewAdapter

    companion object{
        var  musicPlaylist:MusicPlaylist = MusicPlaylist() // made object of the class MusicPlaylist in Music.kt
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Base_Theme_MusicPlayer)

        binding  = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.playlistRV.setHasFixedSize(true)
        binding.playlistRV.setItemViewCacheSize(13)
        binding.playlistRV.layoutManager = GridLayoutManager(this@PlaylistActivity, 2)
        adapter = PlaylistViewAdapter(this , playlistList = musicPlaylist.ref )
        binding.playlistRV.adapter = adapter


        // so that clicking on back button closes the current activity
        binding.backBtnPLA.setOnClickListener {
            finish()
        }
        // clicking on + icon will create a new playlist
        binding.addPlaylistBtnPA.setOnClickListener {
            customAlertDialog()
        }

    }
    private fun customAlertDialog(){
        val customDialog = LayoutInflater.from(this@PlaylistActivity).
        inflate(R.layout.add_playlist_dialog, binding.root, false)
        val binder = AddPlaylistDialogBinding.bind(customDialog)

        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(customDialog)
            .setTitle("Playlist Name")
            .setPositiveButton("Add"){dialog, _->
                val playListName = binder.playlistNameD.text
                val createdBy = binder.yourName.text
                if(playListName!=null && createdBy != null)
                    if(playListName.isNotEmpty() && createdBy.isNotEmpty()){
                        addPlaylist(playListName.toString(), createdBy.toString())
                    }
                dialog.dismiss()
            }.show()
    }
    private fun addPlaylist(name: String, createdBy: String){
        // to check if already the similar playlist exists
        var playlistExists = false
        for(i in musicPlaylist.ref){
            if(name.equals(i.name)){
                playlistExists = true
                break
            }
        }
        if(playlistExists){
            Toast.makeText(this,"Playlist already Exists !!", Toast.LENGTH_SHORT).show()

        }
        else{
            val tempPlaylist = Playlist()
            tempPlaylist.name = name
            tempPlaylist.playlist = ArrayList()
            tempPlaylist.createdBy = createdBy
            val calendar = Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn = sdf.format(calendar)
            musicPlaylist.ref.add(tempPlaylist)
            adapter.refreshPlaylist()
        }
     }
// because playlist icon sets only when we go back to MainActivity and come back
    override fun onResume() {
        super.onResume()
        adapter.notifyDataSetChanged()

    }
}