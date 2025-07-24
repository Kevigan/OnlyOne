package com.example.onlyone.di

import android.content.Context
import androidx.room.Room
import com.example.dao.FriendDao
import com.example.dao.MessageDao
import com.example.dao.SwipeDao
import com.example.onlyone.repos.AppDatabase
import com.example.onlyone.repos.ChatRepository
import com.example.onlyone.repos.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideUserRepository(
        db: FirebaseFirestore,
        friendDao: FriendDao,
        messageDao: MessageDao,
        swipeDao: SwipeDao
    ): UserRepository {
        return UserRepository(db, friendDao, messageDao, swipeDao)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        db: FirebaseFirestore,
        messageDao: MessageDao
    ): ChatRepository {
        return ChatRepository(db, messageDao)
    }
}
