package com.example.loveyapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.loveyapp.data.local.entity.Diary

@Dao
interface DiaryDao {
    @Insert
    suspend fun insert(diary: Diary): Long

    @Query("SELECT * FROM diary ORDER BY date DESC")
    suspend fun getAll(): List<Diary>

    @Query("SELECT * FROM diary WHERE notebook_name = :notebookName ORDER BY date DESC")
    suspend fun findByNotebookName(notebookName: String): List<Diary>

    @Query("SELECT * FROM diary WHERE tags LIKE '%' || :tag || '%' ORDER BY date DESC")
    suspend fun findByTag(tag: String): List<Diary>

    @Query("SELECT * FROM diary WHERE date = :date ORDER BY date DESC")
    suspend fun findByDate(date: String): List<Diary>

    @Query("SELECT * FROM diary WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): Diary?

    @Update
    suspend fun update(diary: Diary)

    @Query("DELETE FROM diary WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM diary WHERE notebook_name = :notebookName")
    suspend fun deleteByNotebookName(notebookName: String)

    @Insert
    suspend fun insertAll(diaries: List<Diary>)

    @Query("DELETE FROM diary")
    suspend fun deleteAll()

    @Query("DELETE FROM diary WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Long>)
}