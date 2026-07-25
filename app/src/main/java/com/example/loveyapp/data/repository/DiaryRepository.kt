package com.example.loveyapp.data.repository

import com.example.loveyapp.data.local.database.LoveYaDatabaseFactory
import com.example.loveyapp.data.local.entity.Diary
import com.example.loveyapp.security.AuthService
import javax.inject.Inject

class DiaryRepository @Inject constructor(
    private val databaseFactory: LoveYaDatabaseFactory,
    private val authService: AuthService
) {
    private val database get() = databaseFactory.createDatabase(authService.currentUsername ?: "")

    suspend fun addDiary(diary: Diary): Long? {
        return try {
            database.diaryDao().insert(diary)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteDiary(id: Long): Boolean {
        return try {
            database.diaryDao().deleteById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateDiary(diary: Diary): Boolean {
        return try {
            database.diaryDao().update(diary.copy(updatedAt = System.currentTimeMillis()))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllDiaries(): List<Diary> {
        return try {
            database.diaryDao().getAll()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDiariesByNotebook(notebookName: String): List<Diary> {
        return try {
            database.diaryDao().findByNotebookName(notebookName)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDiariesByTag(tag: String): List<Diary> {
        return try {
            database.diaryDao().findByTag(tag)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDiariesByDate(date: String): List<Diary> {
        return try {
            database.diaryDao().findByDate(date)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDiaryById(id: Long): Diary? {
        return try {
            database.diaryDao().findById(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteDiaries(ids: List<Long>): Boolean {
        return try {
            database.diaryDao().deleteByIds(ids)
            true
        } catch (e: Exception) {
            false
        }
    }
}