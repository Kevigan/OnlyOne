package com.example.onlyone.di

import android.content.Context
import androidx.room.Room
import com.example.dao.FriendDao
import com.example.dao.MessageDao
import com.example.dao.UserSettingsDao
import com.example.onlyone.repos.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "onlyone_db"
        )
            .fallbackToDestructiveMigration() // ✅ Add this line here
            .build()
    }

    @Provides
    fun provideMessageDao(db: AppDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideFriendDao(db: AppDatabase): FriendDao = db.friendDao()

    @Provides
    fun provideUserSettingsDao(db: AppDatabase): UserSettingsDao = db.userSettingsDao()

}


