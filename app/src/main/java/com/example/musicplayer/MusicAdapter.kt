package com.example.musicplayer
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.MusicViewBinding


class MusicAdapter (private val context:Context, private var musicList: ArrayList<Music>, private val playlistDetails: Boolean = false ,
private val selectionActivity: Boolean = false):
    RecyclerView.Adapter<MusicAdapter.MyHolder>()
{
    class MyHolder(binding: MusicViewBinding): RecyclerView.ViewHolder(binding.root)
    {
        val title = binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration  = binding.songDuration
        // for clicking on a song brings us to player activity
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
       return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration) // formatDuration is defined in music.kt
        Glide.with(context)
            .load(musicList[position].artURI)
            .apply(RequestOptions().placeholder(R.mipmap.music_player_icon).centerCrop())
            .into(holder.image)
        // code for bringing us to player activity after clicking on a song

        when{
            playlistDetails ->{  // i.e if playlist details is true or it exists
                holder.root.setOnClickListener {
                    sendIntent(ref = "PlaylistDetailsAdapter", pos = position)
                }
            }
            selectionActivity ->{
                holder.root.setOnClickListener {
                    if(addSong(musicList[position]))
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.cool_pink))
                    else
                        holder.root.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
                }
            }
            else ->{ holder.root.setOnClickListener {
                when{
                    musicList[position].id == PlayerActivity.nowPlayingID ->
                        sendIntent(ref = "NowPlaying", pos = PlayerActivity.songPosition)
                    else->sendIntent(ref = "MusicAdapter", pos = position)
                }
            }
            }
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }
    private fun sendIntent(ref: String , pos:Int){
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent,null)
    }

    // if song exists then remove the same song from playlist , otherwise add song to playlist
    private fun addSong(song : Music):Boolean{
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPosition].playlist.forEachIndexed{index, music ->
            if(song.id == music.id){
                PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPosition].playlist.removeAt(index)
                return false
            }
        }
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPosition].playlist.add(song)
        return true

    }
    // to refresh playlist after songs are removed
    fun refreshPlaylist(){
        musicList = ArrayList()
        musicList = PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPosition].playlist
        notifyDataSetChanged()
    }
}