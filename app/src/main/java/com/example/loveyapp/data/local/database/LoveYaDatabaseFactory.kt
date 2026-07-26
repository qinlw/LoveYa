package com.example.loveyapp.data.local.database

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.room.Room
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoveYaDatabaseFactory @Inject constructor(@ApplicationContext private val context: Context) {
    private val databaseCache = mutableMapOf<String, LoveYaDatabase>()
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("LoveYa_Prefs", Context.MODE_PRIVATE)
    private var customStorageUri: Uri? = null

    init {
        loadCustomStorageUri()
    }

    private fun loadCustomStorageUri() {
        val uriString = sharedPreferences.getString("custom_storage_uri", null)
        customStorageUri = if (uriString != null) Uri.parse(uriString) else null
    }

    fun createDatabase(username: String): LoveYaDatabase {
        return databaseCache.getOrPut(username) {
            val dbName = "loveya_${username}.db"
            val defaultDbFile = context.getDatabasePath(dbName)

            if (customStorageUri != null) {
                copyFromCustomStorageIfExists(dbName, defaultDbFile)
            }

            val builder = Room.databaseBuilder(
                context.applicationContext,
                LoveYaDatabase::class.java,
                defaultDbFile.absolutePath
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)

            val db = builder.build()

            if (customStorageUri != null) {
                copyToCustomStorage(dbName, defaultDbFile)
            }

            db
        }
    }

    private fun copyFromCustomStorageIfExists(dbName: String, defaultDbFile: File) {
        try {
            val documentFile = DocumentFile.fromTreeUri(context, customStorageUri!!)
            val externalDbFile = documentFile?.findFile(dbName)
            if (externalDbFile != null && !defaultDbFile.exists()) {
                context.contentResolver.openInputStream(externalDbFile.uri)?.use { inputStream ->
                    FileOutputStream(defaultDbFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun copyToCustomStorage(dbName: String, dbFile: File) {
        try {
            if (customStorageUri != null && dbFile.exists()) {
                val documentFile = DocumentFile.fromTreeUri(context, customStorageUri!!)
                var externalDbFile = documentFile?.findFile(dbName)
                if (externalDbFile == null) {
                    externalDbFile = documentFile?.createFile("application/octet-stream", dbName)
                }
                if (externalDbFile != null) {
                    context.contentResolver.openOutputStream(externalDbFile.uri)?.use { outputStream ->
                        FileInputStream(dbFile).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setCustomStorageUri(uri: Uri?) {
        customStorageUri = uri
        sharedPreferences.edit().putString("custom_storage_uri", uri?.toString()).apply()
    }

    fun getCustomStorageUri(): Uri? = customStorageUri

    fun getCurrentStoragePath(username: String): String {
        return if (customStorageUri != null) {
            "${customStorageUri!!.path}/loveya_${username}.db"
        } else {
            context.getDatabasePath("loveya_${username}.db").absolutePath
        }
    }

    fun closeDatabase(username: String) {
        val db = databaseCache.remove(username)
        db?.let {
            val dbFile = context.getDatabasePath("loveya_${username}.db")
            copyToCustomStorage("loveya_${username}.db", dbFile)
            it.close()
        }
    }

    fun closeAll() {
        databaseCache.forEach { (username, db) ->
            val dbFile = context.getDatabasePath("loveya_${username}.db")
            copyToCustomStorage("loveya_${username}.db", dbFile)
            db.close()
        }
        databaseCache.clear()
    }
}
