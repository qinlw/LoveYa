package com.example.loveyapp.data.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authService: com.example.loveyapp.security.AuthService,
    private val databaseFactory: com.example.loveyapp.data.local.database.LoveYaDatabaseFactory
) {

    fun getAllUsers(): List<String> {
        val users = mutableListOf<String>()
        try {
            val dbDir = context.getDatabasePath("dummy.db").parentFile
            dbDir?.listFiles()?.forEach { file ->
                val fileName = file.name
                if (fileName.endsWith(".db") && fileName.startsWith("loveya_")) {
                    val username = fileName.substring(7).removeSuffix(".db")
                    if (username.isNotEmpty()) {
                        users.add(username)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return users.sorted()
    }

    fun switchUser(username: String): Boolean {
        return try {
            authService.logout()
            authService.login(username)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun deleteUser(username: String): Boolean {
        return try {
            databaseFactory.closeDatabase(username)

            val dbPath = context.getDatabasePath("loveya_${username}.db")
            val dbShmPath = context.getDatabasePath("loveya_${username}.db-shm")
            val dbWalPath = context.getDatabasePath("loveya_${username}.db-wal")

            dbPath.delete()
            dbShmPath.delete()
            dbWalPath.delete()

            if (authService.currentUsername == username) {
                authService.logout()
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isUserExists(username: String): Boolean {
        return getAllUsers().contains(username)
    }
}