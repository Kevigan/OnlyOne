package com.example.onlyone.di

import com.example.onlyone.repos.ChatRepository
import com.example.onlyone.repos.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
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
    fun provideUserRepository(db: FirebaseFirestore): UserRepository {
        return UserRepository(db)
    }

    @Provides
    @Singleton
    fun provideChatRepository(db: FirebaseFirestore): ChatRepository {
        return ChatRepository(db)
    }
}
