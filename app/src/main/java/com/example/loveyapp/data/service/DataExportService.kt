package com.example.loveyapp.data.service

import android.net.Uri
import com.example.loveyapp.data.local.database.LoveYaDatabaseFactory
import com.example.loveyapp.data.local.entity.AnniversaryConfig
import com.example.loveyapp.data.local.entity.DataBook
import com.example.loveyapp.data.local.entity.Diary
import com.example.loveyapp.data.local.entity.UserInfo
import com.example.loveyapp.security.SafManager
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExportService @Inject constructor(
    private val databaseFactory: LoveYaDatabaseFactory,
    private val safManager: SafManager,
    private val gson: Gson
) {

    data class ExportData(
        @SerializedName("version") val version: String = "1.2",
        @SerializedName("exportDate") val exportDate: String,
        @SerializedName("userInfo") val userInfo: UserInfo?,
        @SerializedName("diaries") val diaries: List<Diary>,
        @SerializedName("dataBooks") val dataBooks: List<DataBook>,
        @SerializedName("anniversaries") val anniversaries: List<AnniversaryConfig>
    )

    suspend fun exportUserData(username: String): String {
        return try {
            val database = databaseFactory.createDatabase(username)
            val userInfo = database.userInfoDao().findByUsername(username)
            val diaries = database.diaryDao().getAll()
            val dataBooks = database.dataBookDao().getAll()
            val anniversaries = database.anniversaryConfigDao().getAll()

            val exportData = ExportData(
                exportDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                userInfo = userInfo,
                diaries = diaries,
                dataBooks = dataBooks,
                anniversaries = anniversaries
            )

            gson.toJson(exportData)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    suspend fun exportAndSave(username: String, targetDirectoryUri: Uri): Boolean {
        return try {
            val jsonData = exportUserData(username)
            if (jsonData.isEmpty()) return false

            val fileName = "lovey_backup_${username}_${System.currentTimeMillis()}.json"
            safManager.saveFileToSaf(targetDirectoryUri, fileName, jsonData.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}