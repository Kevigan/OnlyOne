package com.example.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.onlyone.data.LocalFriend
import kotlinx.coroutines.flow.Flow

@Dao
interface FriendDao {
    @Query("SELECT * FROM LocalFriend")
    fun getAllFriends(): Flow<List<LocalFriend>>

    @Query("SELECT * FROM LocalFriend")
    suspend fun getAllFriendsNow(): List<LocalFriend>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(friends: List<LocalFriend>)

    @Query("DELETE FROM LocalFriend")
    suspend fun clearFriends()

    @Query("DELETE FROM localfriend WHERE uid = :uid")
    suspend fun deleteByUid(uid: String)

    @Query("SELECT * FROM LocalFriend WHERE uid = :uid LIMIT 1")
    suspend fun getFriendByUid(uid: String): LocalFriend?

}
