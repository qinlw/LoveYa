package com.example.loveyapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.loveyapp.data.local.entity.AnniversaryConfig

@Dao
interface AnniversaryConfigDao {
    @Insert
    suspend fun insert(anniversaryConfig: AnniversaryConfig): Long

    @Query("SELECT * FROM anniversary_config WHERE enabled = 1 ORDER BY display_order")
    suspend fun getAllEnabled(): List<AnniversaryConfig>

    @Query("SELECT * FROM anniversary_config ORDER BY display_order")
    suspend fun getAll(): List<AnniversaryConfig>

    @Query("SELECT * FROM anniversary_config WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): AnniversaryConfig?

    @Query("SELECT * FROM anniversary_config WHERE name = :name LIMIT 1")
    suspend fun findByName(name: String): AnniversaryConfig?

    @Update
    suspend fun update(anniversaryConfig: AnniversaryConfig)

    @Query("DELETE FROM anniversary_config WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert
    suspend fun insertAll(anniversaries: List<AnniversaryConfig>)

    @Query("DELETE FROM anniversary_config")
    suspend fun deleteAll()
}