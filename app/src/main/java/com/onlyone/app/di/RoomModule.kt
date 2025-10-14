package com.onlyone.app.di

import android.content.Context
import androidx.room.Room
import com.onlyone.app.dao.FavoriteMessageDao
import com.onlyone.app.dao.FriendDao
import com.onlyone.app.dao.MessageDao
import com.onlyone.app.dao.SavedChainsDao
import com.onlyone.app.dao.UserSettingsDao
import com.onlyone.app.repos.AppDatabase
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

    @Provides
    fun provideFavoriteMessageDao(db: AppDatabase): FavoriteMessageDao = db.favoriteMessageDao()

    @Provides
    fun provideSavedChainsDao(db: AppDatabase): SavedChainsDao = db.savedChainsDao()
}


