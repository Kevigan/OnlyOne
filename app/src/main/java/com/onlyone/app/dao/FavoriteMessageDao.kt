package com.onlyone.app.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.onlyone.app.data.LocalFavoriteMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteMessageDao {
    @Query("SELECT * FROM LocalFavoriteMessage ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<LocalFavoriteMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: LocalFavoriteMessage)

    @Query("DELETE FROM LocalFavoriteMessage WHERE id = :messageId")
    suspend fun deleteById(messageId: String)

    @Query("SELECT COUNT(*) FROM LocalFavoriteMessage WHERE id = :messageId")
    suspend fun countById(messageId: String): Int

    @Query("DELETE FROM LocalFavoriteMessage")
    suspend fun clear()
}
