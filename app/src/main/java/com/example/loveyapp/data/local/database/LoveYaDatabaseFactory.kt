package com.example.loveyapp.data.local.database

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.room.RoomDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoveYaDatabaseFactory @Inject constructor(@ApplicationContext private val context: Context) {
    private val databaseCache = mutableMapOf<String, LoveYaDatabase>()
    private var customStorageUri: Uri? = null

    fun createDatabase(username: String): LoveYaDatabase {
        return databaseCache.getOrPut(username) {
            val dbName = "loveya_${username}.db"
            val builder = Room.databaseBuilder(
                context.applicationContext,
                LoveYaDatabase::class.java,
                dbName
            )
                .fallbackToDestructiveMigration()

            customStorageUri?.let { uri ->
                builder.setQueryExecutor { runnable ->
                    Thread(runnable).start()
                }
            }

            builder.build()
        }
    }

    fun setCustomStorageUri(uri: Uri?) {
        customStorageUri = uri
    }

    fun getCustomStorageUri(): Uri? = customStorageUri

    fun getDefaultDatabasePath(username: String): String {
        return context.getDatabasePath("loveya_${username}.db").absolutePath
    }

    fun closeDatabase(username: String) {
        databaseCache.remove(username)?.close()
    }

    fun closeAll() {
        databaseCache.values.forEach { it.close() }
        databaseCache.clear()
    }
}