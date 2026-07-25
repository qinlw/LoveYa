package com.example.loveyapp.data.service

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import java.io.*

@Singleton
class DataBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val databaseFactory: com.example.loveyapp.data.local.database.LoveYaDatabaseFactory
) {
    fun exportDatabase(username: String, uri: Uri): Boolean {
        return try {
            val dbPath = databaseFactory.getDefaultDatabasePath(username)
            val dbFile = File(dbPath)
            if (!dbFile.exists()) {
                return false
            }

            val inputStream = FileInputStream(dbFile)
            val outputStream = context.contentResolver.openOutputStream(uri)

            outputStream?.use { out ->
                inputStream.use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        out.write(buffer, 0, bytesRead)
                    }
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun importDatabase(username: String, uri: Uri): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val dbPath = databaseFactory.getDefaultDatabasePath(username)
            val dbFile = File(dbPath)

            inputStream?.use { input ->
                FileOutputStream(dbFile).use { out ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        out.write(buffer, 0, bytesRead)
                    }
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getDefaultBackupPath(): String {
        val customUri = databaseFactory.getCustomStorageUri()
        if (customUri != null) {
            return customUri.toString()
        }
        return context.getExternalFilesDir(null)?.absolutePath ?: context.filesDir.absolutePath
    }

    fun getBackupFileName(username: String): String {
        return "loveya_backup_${username}_${System.currentTimeMillis()}.db"
    }
}
