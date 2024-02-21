package com.example.musicplayer

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.databinding.ActivitySelectionBinding

class SelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectionBinding
    private lateinit var adapter: MusicAdapter
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setTheme(R.color.cool_pink)
        setContentView(binding.root)

        binding.selectionRV.setItemViewCacheSize(10)
        binding.selectionRV.setHasFixedSize(true)
        binding.selectionRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this,MainActivity.MusicListMA, selectionActivity = true)
        binding.selectionRV.adapter = adapter  // now initializing the adapter
        binding.backBtnSA.setOnClickListener {
            finish()
        }




    }
}