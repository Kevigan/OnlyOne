package com.example.onlyone.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity
data class LocalFriend(
    @PrimaryKey val uid: String,
    val username: String,
    val moodStatus: String,
    val avatarId: Int,
    val points: Int,
    val achievementCount: Int = 0,
    val favouriteMessage: FavouriteMessage? = null
)

// FavouriteMessageConverter.kt
class FavouriteMessageConverter {
    @TypeConverter
    fun toJson(value: FavouriteMessage?): String? =
        value?.let { com.google.gson.Gson().toJson(it) }

    @TypeConverter
    fun fromJson(json: String?): FavouriteMessage? =
        json?.let { com.google.gson.Gson().fromJson(it, FavouriteMessage::class.java) }
}
