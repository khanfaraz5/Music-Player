package com.example.musicplayer

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.databinding.FavoriteViewBinding

class favoriteAdapter (private val context: Context, private val musicList: ArrayList<Music>):
    RecyclerView.Adapter<favoriteAdapter.MyHolder>()
{
    class MyHolder(binding: FavoriteViewBinding): RecyclerView.ViewHolder(binding.root)
    {
        val image = binding.songImgFV
        val name = binding.songNameFV
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(FavoriteViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {

        holder.name.text = musicList[position].title
        Glide.with(context)
            .load(musicList[position].artURI)
            .apply(RequestOptions().placeholder(R.mipmap.music_player_icon).centerCrop())
            .into(holder.image)

        holder.root.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("index", position)
            intent.putExtra("class", "favoriteAdapter")
            ContextCompat.startActivity(context, intent,null)
        }
        }
    }
