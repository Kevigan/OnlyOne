package com.example.onlyone.repos

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.dao.MessageDao
import com.example.onlyone.data.LocalMessage
import com.example.onlyone.data.WrittenTodayEntity

@Database(
    entities = [LocalMessage::class, WrittenTodayEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}
