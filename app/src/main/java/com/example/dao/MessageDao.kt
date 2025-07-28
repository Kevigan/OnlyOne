package com.example.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.onlyone.data.LocalMessage
import com.example.onlyone.data.WrittenTodayEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM LocalMessage WHERE receiverId = :uid ORDER BY timestamp DESC")
    fun getMessagesForUser(uid: String): Flow<List<LocalMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<LocalMessage>)

    @Query("SELECT MAX(timestamp) FROM LocalMessage WHERE receiverId = :uid")
    suspend fun getLastTimestamp(uid: String): Long?

    @Query("DELETE FROM LocalMessage")
    suspend fun clearLocalMessages()

    @Query("SELECT * FROM LocalMessage WHERE id = :messageId LIMIT 1")
    suspend fun getMessageById(messageId: String): LocalMessage?

    @Query("DELETE FROM LocalMessage WHERE timestamp < :cutoff")
    suspend fun deleteExpiredMessages(cutoff: Long)


    //////////////// 💬 Daily write limit tracking /////////////////////
    @Query("SELECT * FROM WrittenTodayEntity")
    suspend fun getWrittenToday(): List<WrittenTodayEntity>

    @Query("SELECT * FROM WrittenTodayEntity WHERE receiverId = :receiverId")
    suspend fun getWrittenEntry(receiverId: String): WrittenTodayEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWrittenEntry(entry: WrittenTodayEntity)

    @Query("DELETE FROM WrittenTodayEntity")
    suspend fun clearWrittenToday()

    @Query("SELECT * FROM WrittenTodayEntity")
    fun observeWrittenToday(): Flow<List<WrittenTodayEntity>>
}
