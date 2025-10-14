package com.onlyone.app.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.onlyone.app.data.LocalFriend
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT * FROM LocalFriend")
    fun getAllFriends(): Flow<List<LocalFriend>>

    @Query("SELECT * FROM LocalFriend")
    suspend fun getAllFriendsNow(): List<LocalFriend>

    // ✅ back: fetch a single friend
    @Query("SELECT * FROM LocalFriend WHERE uid = :uid LIMIT 1")
    suspend fun getFriendByUid(uid: String): LocalFriend?

    // keep for full refresh fallback
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(friends: List<LocalFriend>)

    @Query("DELETE FROM LocalFriend")
    suspend fun clearFriends()

    @Query("DELETE FROM LocalFriend WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)

    // bulk delete (for removedUids)
    @Query("DELETE FROM LocalFriend WHERE uid IN (:uids)")
    suspend fun deleteByUids(uids: List<String>)

    // upserts for delta writes (Room 2.5+)
    @Upsert
    suspend fun upsert(friend: LocalFriend)

    @Upsert
    suspend fun upsertAll(friends: List<LocalFriend>)

    // one-transaction delta apply (no table wipe)
    @Transaction
    suspend fun applyDelta(removedUids: List<String>, upserts: List<LocalFriend>) {
        if (removedUids.isNotEmpty()) deleteByUids(removedUids)
        if (upserts.isNotEmpty()) upsertAll(upserts)
    }
}