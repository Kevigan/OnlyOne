package com.example.onlyone.repos

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dao.FriendDao
import com.example.dao.MessageDao
import com.example.dao.SwipeDao
import com.example.onlyone.data.LocalFriend
import com.example.onlyone.data.LocalMessage
import com.example.onlyone.data.LocalSwipeStatus
import com.example.onlyone.data.WrittenTodayEntity

@Database(
    entities = [LocalMessage::class, WrittenTodayEntity::class, LocalFriend::class, LocalSwipeStatus::class],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun friendDao(): FriendDao
    abstract fun swipeDao(): SwipeDao
}

