package com.example.loveyapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.loveyapp.data.local.entity.UserInfo

@Dao
interface UserInfoDao {
    @Insert
    suspend fun insert(userInfo: UserInfo): Long

    @Query("SELECT * FROM user_info WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserInfo?

    @Query("SELECT * FROM user_info WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): UserInfo?

    @Update
    suspend fun update(userInfo: UserInfo)

    @Query("DELETE FROM user_info WHERE id = :id")
    suspend fun deleteById(id: Long)
}