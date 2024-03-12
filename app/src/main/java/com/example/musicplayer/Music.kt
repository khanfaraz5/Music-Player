package com.example.musicplayer

import java.util.concurrent.TimeUnit

class Music(val id: String, val title: String , val album: String , val artist: String ,val duration: Long = 0,
            val path: String, val artURI: String)
// for storing data of playList
class Playlist{
    lateinit var name: String
    lateinit var playlist:ArrayList<Music>
    lateinit var createdBy: String
    lateinit var createdOn:String
}
// in jSON's shared preferences , it is recommended to reference a class by another class
class MusicPlaylist{
    var ref: ArrayList<Playlist> = ArrayList()
}

fun formatDuration(duration: Long): String{

    // our song length was in milliseconds by default , here we change it to minutes:seconds
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) - minutes*TimeUnit.SECONDS.convert(1,TimeUnit.MINUTES))

    return String.format("%02d:%02d",minutes,seconds)
}

// if user clicks next after the last song or previous button on first song , then the song position i.e song number will be reset as per condition
fun setSongPosition(increment: Boolean){
    // code will run only when repeat button is off
    if(!PlayerActivity.repeat){
        if(increment) {
            if (PlayerActivity.musicListPA.size - 1 == PlayerActivity.songPosition)
                PlayerActivity.songPosition = 0;
            else {
                ++PlayerActivity.songPosition
            }
        }
        else{
            if(0 == PlayerActivity.songPosition)
                PlayerActivity.songPosition = PlayerActivity.musicListPA.size-1;
            else {
                --PlayerActivity.songPosition
            }

        }
    }

}

//// to check if current song or a particular song is in favorite list or not
//fun favoriteChecker(id: String): Int{
//    PlayerActivity.isFavorite = false
//    FavoriteActivity.favoriteSongs.forEachIndexed { index, music ->
//        if(id == music.id){
//            PlayerActivity.isFavorite = true
//            return index
//        }
//
//    }
//    return -1
//}