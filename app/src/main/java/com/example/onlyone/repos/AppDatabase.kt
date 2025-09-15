package com.example.onlyone.repos

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.dao.FavoriteMessageDao
import com.example.dao.FriendDao
import com.example.dao.MessageDao
import com.example.dao.UserSettingsDao
import com.example.onlyone.data.*

@Database(
    entities = [
        LocalMessage::class,
        WrittenTodayEntity::class,
        LocalFriend::class,
        LocalUserSettings::class,
        LocalFavoriteMessage::class        // NEW
    ],
    version = 12                          // bumped from 9 -> 10
)
@TypeConverters(FavouriteMessageConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun friendDao(): FriendDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun favoriteMessageDao(): FavoriteMessageDao // NEW
}


