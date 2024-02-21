package com.example.musicplayer

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.databinding.ActivityMainBinding
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding // for viewBinding
    private lateinit var musicAdapter: MusicAdapter

    companion object{
       lateinit var MusicListMA: ArrayList<Music>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        requestRuntimePermission()

        setTheme(R.style.Base_Theme_MusicPlayer)
        binding  = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(requestRuntimePermission()){
            initializeLayout()
            // for retrieving favorites data using shared preferences (since songs disappeared from favorites section after closing app)
            FavoriteActivity.favoriteSongs = ArrayList()
            val editor = getSharedPreferences("Favorites", MODE_PRIVATE) // MODE_PRIVATE so that no other apps can access the data
            val jsonString = editor.getString("FavoriteSongs",null)
            val typeToken = object :TypeToken<ArrayList<Music>>(){}.type

            if(jsonString !=null){ // bcuz it's not necessary that favorites will contain songs always
                val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString , typeToken)
                FavoriteActivity.favoriteSongs.addAll(data)
            }

            PlaylistActivity.musicPlaylist = MusicPlaylist()
            val jsonStringPlaylist = editor.getString("MusicPlaylist",null)

            if(jsonStringPlaylist !=null){ // bcuz it's not necessary that favorites will contain songs always
                val dataPlaylist: MusicPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylist , MusicPlaylist::class.java)
                PlaylistActivity.musicPlaylist = dataPlaylist
            }
        }

        binding.shuffleBtn.setOnClickListener {

            val intent = Intent(this@MainActivity, PlayerActivity::class.java)// for going to
            // PlayerActivity when clicked on shuffle button
            intent.putExtra("index", 0)
            intent.putExtra("class", "MainActivity")
            startActivity(intent)
        }

        binding.favoritesBtn.setOnClickListener {

            // for going to
            // favoriteActivity when clicked on favorites button
            startActivity(Intent(this@MainActivity, FavoriteActivity::class.java))
        }

        binding.playlistBtn.setOnClickListener {

            // for going to
            // favoriteActivity when clicked on favorites button
            startActivity(Intent(this@MainActivity, PlaylistActivity::class.java))
        }

    }
    // for requesting permission
    private fun requestRuntimePermission(): Boolean{
        if(ActivityCompat.checkSelfPermission(this , android.Manifest.permission.READ_MEDIA_AUDIO)
            !=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this , arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO), 13)
            return false
        }
        return true
    }

    // for handling permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    )
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 13)
        {
            // grantResults is an array which stores permissions, 1st permission is stored at index 0
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText( this,"Permission granted !!", Toast.LENGTH_SHORT).show()
                initializeLayout()
            }
            else{
                ActivityCompat.requestPermissions(this , arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO), 13)
            }
        }
    }
    private fun initializeLayout(){

        MusicListMA = getAllAudio()
        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(13)
        binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
        musicAdapter = MusicAdapter(this@MainActivity , MusicListMA)
        binding.musicRV.adapter = musicAdapter
        binding.totalSongs.text = "Total Songs : "+musicAdapter.itemCount // for returning total songs
    }

    @SuppressLint("Range")
    private fun getAllAudio(): ArrayList<Music>{
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC+ "!=0"

        // now for fetching music from mobile , you need cursor which fetches the data from storage
        // so first we define what type of data needs to be fetched by the help of projection
        // and then pass that to cursor.

        val projection = arrayOf(MediaStore.Audio.Media._ID ,MediaStore.Audio.Media.TITLE ,
            MediaStore.Audio.Media.ALBUM ,MediaStore.Audio.Media.ARTIST ,MediaStore.Audio.Media.DURATION ,
            MediaStore.Audio.Media.DATE_ADDED ,MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID)
        // There are two content URI , external and internal , external is the one which fetches the audio
        // while internal fetches only the builtin files such as ringtones

        val cursor = this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI ,
            projection ,selection,null ,MediaStore.Audio.Media.DATE_ADDED,null )

        if(cursor!=null)
        {
            if(cursor.moveToFirst())
            {
                do{
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC  = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID.toString()))
                    // for fetching song cover photo from device
                    val uri = Uri.parse("content://media/external/audio/albumart") // for fetching song cover photo from device
                    val artUriC = Uri.withAppendedPath(uri , albumIdC.toString()).toString()
                    // creating the music object
                    val music = Music(id = idC, title = titleC , album = albumC, artist = artistC ,
                        path = pathC , duration = durationC,  artURI = artUriC)

                    val file = File(music.path)
                    if(file.exists())
                    {
                        tempList.add(music)
                    }
                }
                while (cursor.moveToNext())
                cursor.close()
            }
        }

        return tempList


    }

    // if song is paused and user has closed the app , then app should be closed completely from foreground and background

    override fun onDestroy() {
        super.onDestroy()
        if(!PlayerActivity.isPlaying && PlayerActivity.musicService != null){
            PlayerActivity.musicService!!.stopForeground(true)
            PlayerActivity.musicService!!.mediaPlayer!!.release()
            PlayerActivity.musicService = null
            exitProcess(1)

        }

    }

    override fun onResume() {
        super.onResume()
        // for storing favorites data using shared preferences (since songs disappeared from favorites section after closing app)
        val editor = getSharedPreferences("Favorites", MODE_PRIVATE).edit() // MODE_PRIVATE so that no other apps can access the data
        val jsonString = GsonBuilder().create().toJson(FavoriteActivity.favoriteSongs)
        editor.putString("FavoriteSongs",jsonString)
        // for storing playlist data using shared preferences (since songs disappeared from favorites section after closing app)
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist",jsonStringPlaylist)
        editor.apply()
    }
}