package com.example.loveyapp.data.repository

import com.example.loveyapp.data.local.database.LoveYaDatabaseFactory
import com.example.loveyapp.data.local.entity.DataBook
import com.example.loveyapp.security.AuthService
import javax.inject.Inject

class DataBookRepository @Inject constructor(
    private val databaseFactory: LoveYaDatabaseFactory,
    private val authService: AuthService
) {
    private val database get() = databaseFactory.createDatabase(authService.currentUsername ?: "")

    suspend fun addDataBook(dataBook: DataBook): Long? {
        return try {
            database.dataBookDao().insert(dataBook)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteDataBook(id: Long): Boolean {
        return try {
            database.dataBookDao().deleteById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateDataBook(dataBook: DataBook): Boolean {
        return try {
            database.dataBookDao().update(dataBook.copy(updatedAt = System.currentTimeMillis()))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllDataBooks(): List<DataBook> {
        return try {
            database.dataBookDao().getAll()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getDataBookById(id: Long): DataBook? {
        return try {
            database.dataBookDao().findById(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteDataBooks(ids: List<Long>): Boolean {
        return try {
            database.dataBookDao().deleteByIds(ids)
            true
        } catch (e: Exception) {
            false
        }
    }
}
