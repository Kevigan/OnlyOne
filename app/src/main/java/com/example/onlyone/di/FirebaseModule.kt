package com.example.onlyone.di
import com.example.dao.FavoriteMessageDao
import com.example.dao.FriendDao
import com.example.dao.MessageDao
import com.example.dao.UserSettingsDao
import com.example.onlyone.repos.ChatRepository
import com.example.onlyone.repos.UserInventoryRepo
import com.example.onlyone.repos.userRepos.UserRepository
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
    fun provideChatRepository(
        db: FirebaseFirestore,
        messageDao: MessageDao,
        favoriteMessageDao: FavoriteMessageDao
    ): ChatRepository {
        return ChatRepository(db, messageDao, favoriteMessageDao)
    }

    @Provides
    @Singleton
    fun provideUserInventoryRepo(
        db: FirebaseFirestore
    ): UserInventoryRepo = UserInventoryRepo(db)
}
