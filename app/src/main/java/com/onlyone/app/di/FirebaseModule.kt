package com.onlyone.app.di
import com.onlyone.app.dao.FavoriteMessageDao
import com.onlyone.app.dao.MessageDao
import com.onlyone.app.repos.ChatRepository
import com.onlyone.app.repos.UserInventoryRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
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

    @Provides @Singleton @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

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

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher