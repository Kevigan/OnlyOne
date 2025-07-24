package com.example.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.onlyone.data.LocalSwipeStatus

@Dao
interface SwipeDao {
    @Query("SELECT * FROM LocalSwipeStatus WHERE uid = :uid")
    suspend fun getSwipeStatus(uid: String): LocalSwipeStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwipeStatus(status: LocalSwipeStatus)
}
