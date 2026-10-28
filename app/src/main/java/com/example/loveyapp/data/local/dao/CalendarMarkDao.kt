package com.example.loveyapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.loveyapp.data.local.entity.CalendarMark

@Dao
interface CalendarMarkDao {
    @Insert
    suspend fun insert(mark: CalendarMark): Long

    @Query("SELECT * FROM calendar_mark ORDER BY date ASC")
    suspend fun getAll(): List<CalendarMark>

    @Query("DELETE FROM calendar_mark WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert
    suspend fun insertAll(marks: List<CalendarMark>)

    @Query("DELETE FROM calendar_mark")
    suspend fun deleteAll()
}
