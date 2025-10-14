package com.onlyone.app.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.onlyone.app.data.SavedChainEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedChainsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavedChainEntity)

    @Query("SELECT * FROM saved_chains ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<SavedChainEntity>>

    @Query("DELETE FROM saved_chains WHERE chainId = :chainId")
    suspend fun delete(chainId: String)

    @Query("SELECT * FROM saved_chains WHERE chainId = :chainId LIMIT 1")
    suspend fun get(chainId: String): SavedChainEntity?
}