package com.example.loveyapp.security

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthService @Inject constructor(
    private val keyManager: KeyManager,
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences by lazy {
        keyManager.getEncryptedSharedPreferences("auth_prefs")
    }

    private var _currentUsername: String? = null

    val currentUsername: String?
        get() = _currentUsername ?: prefs.getString("current_username", null).also {
            _currentUsername = it
        }

    val isLoggedIn: Boolean
        get() = currentUsername != null

    fun login(username: String) {
        _currentUsername = username
        prefs.edit().putString("current_username", username).apply()
    }

    fun logout() {
        _currentUsername = null
        prefs.edit().remove("current_username").apply()
    }

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

    fun deleteUser(username: String): Boolean {
        return try {
            val dbPath = context.getDatabasePath("loveya_${username}.db")
            val dbShmPath = context.getDatabasePath("loveya_${username}.db-shm")
            val dbWalPath = context.getDatabasePath("loveya_${username}.db-wal")

            val deleted = dbPath.delete() && dbShmPath.delete() && dbWalPath.delete()

            if (currentUsername == username) {
                logout()
            }

            deleted
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}