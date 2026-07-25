package com.example.loveyapp.data.repository

import com.example.loveyapp.data.local.database.LoveYaDatabaseFactory
import com.example.loveyapp.data.local.entity.AnniversaryConfig
import com.example.loveyapp.security.AuthService
import javax.inject.Inject

class AnniversaryRepository @Inject constructor(
    private val databaseFactory: LoveYaDatabaseFactory,
    private val authService: AuthService
) {
    private val database get() = databaseFactory.createDatabase(authService.currentUsername ?: "")

    suspend fun addAnniversary(config: AnniversaryConfig): Long? {
        return try {
            database.anniversaryConfigDao().insert(config)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteAnniversary(id: Long): Boolean {
        return try {
            database.anniversaryConfigDao().deleteById(id)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateAnniversary(config: AnniversaryConfig): Boolean {
        return try {
            database.anniversaryConfigDao().update(config.copy(updatedAt = System.currentTimeMillis()))
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllAnniversaries(): List<AnniversaryConfig> {
        return try {
            database.anniversaryConfigDao().getAll()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getEnabledAnniversaries(): List<AnniversaryConfig> {
        return try {
            database.anniversaryConfigDao().getAllEnabled()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAnniversaryById(id: Long): AnniversaryConfig? {
        return try {
            database.anniversaryConfigDao().findById(id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAnniversaryByName(name: String): AnniversaryConfig? {
        return try {
            database.anniversaryConfigDao().findByName(name)
        } catch (e: Exception) {
            null
        }
    }
}