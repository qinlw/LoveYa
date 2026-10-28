package com.example.loveyapp.data.repository

import com.example.loveyapp.data.local.database.LoveYaDatabaseFactory
import com.example.loveyapp.data.local.entity.CalendarMark
import com.example.loveyapp.security.AuthService
import javax.inject.Inject

class CalendarMarkRepository @Inject constructor(
    private val databaseFactory: LoveYaDatabaseFactory,
    private val authService: AuthService
) {
    private val database get() = databaseFactory.createDatabase(authService.currentUsername ?: "")

    suspend fun getAllMarks(): List<CalendarMark> {
        return try {
            database.calendarMarkDao().getAll()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addMark(mark: CalendarMark): Long? {
        return try {
            database.calendarMarkDao().insert(mark)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteMark(id: Long): Boolean {
        return try {
            database.calendarMarkDao().deleteById(id)
            true
        } catch (e: Exception) {
            false
        }
    }
}
