package com.onlyone.app.repos

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.onlyone.app.dao.FavoriteMessageDao
import com.onlyone.app.dao.FriendDao
import com.onlyone.app.dao.MessageDao
import com.onlyone.app.dao.SavedChainsDao
import com.onlyone.app.dao.UserSettingsDao
import com.onlyone.app.data.FavouriteMessageConverter
import com.onlyone.app.data.LocalFavoriteMessage
import com.onlyone.app.data.LocalFriend
import com.onlyone.app.data.LocalMessage
import com.onlyone.app.data.LocalUserSettings
import com.onlyone.app.data.SavedChainEntity
import com.onlyone.app.data.WrittenTodayEntity

// AppDatabase.kt
@Database(
    entities = [
        LocalMessage::class,
        WrittenTodayEntity::class,
        LocalFriend::class,
        LocalUserSettings::class,
        LocalFavoriteMessage::class,
        SavedChainEntity::class,
    ],
    version = 16,
    exportSchema = true
)
@TypeConverters(FavouriteMessageConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun friendDao(): FriendDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun favoriteMessageDao(): FavoriteMessageDao
    abstract fun savedChainsDao(): SavedChainsDao
}



