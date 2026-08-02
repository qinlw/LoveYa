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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataImportService @Inject constructor(
    private val databaseFactory: LoveYaDatabaseFactory,
    private val safManager: SafManager,
    private val gson: Gson
) {

    data class ImportData(
        @SerializedName("version") val version: String,
        @SerializedName("exportDate") val exportDate: String,
        @SerializedName("userInfo") val userInfo: UserInfo?,
        @SerializedName("diaries") val diaries: List<Diary>,
        @SerializedName("dataBooks") val dataBooks: List<DataBook>,
        @SerializedName("anniversaries") val anniversaries: List<AnniversaryConfig>
    )

    fun importFromFile(fileUri: Uri): ImportResult {
        return try {
            val content = safManager.readFileFromSaf(fileUri)
            if (content == null) {
                return ImportResult(success = false, message = "无法读取文件")
            }
            parseImportData(String(content))
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(success = false, message = "解析失败: ${e.message}")
        }
    }

    /**
     * 云还原入口：直接从 JSON 字符串解析，不依赖 SAF。
     * 与 [importFromFile] 共享 [parseImportData]，逻辑保持一致。
     */
    fun importFromJson(json: String): ImportResult {
        return try {
            parseImportData(json)
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(success = false, message = "解析失败: ${e.message}")
        }
    }

    private fun parseImportData(json: String): ImportResult {
        val importData = gson.fromJson(json, ImportData::class.java)
            ?: return ImportResult(success = false, message = "备份数据为空")
        if (importData.version != "1.0") {
            return ImportResult(success = false, message = "不支持的备份版本")
        }
        if (importData.userInfo == null) {
            return ImportResult(success = false, message = "备份数据中没有用户信息")
        }
        return ImportResult(success = true, data = importData, message = "解析成功")
    }

    suspend fun importToDatabase(username: String, importData: ImportData): Boolean {
        return try {
            val database = databaseFactory.createDatabase(username)

            importData.userInfo?.let {
                val existing = database.userInfoDao().findByUsername(username)
                if (existing != null) {
                    database.userInfoDao().update(it.copy(id = existing.id))
                } else {
                    database.userInfoDao().insert(it)
                }
            }

            if (importData.diaries.isNotEmpty()) {
                database.diaryDao().deleteAll()
                database.diaryDao().insertAll(importData.diaries)
            }

            if (importData.dataBooks.isNotEmpty()) {
                database.dataBookDao().deleteAll()
                database.dataBookDao().insertAll(importData.dataBooks)
            }

            if (importData.anniversaries.isNotEmpty()) {
                database.anniversaryConfigDao().deleteAll()
                database.anniversaryConfigDao().insertAll(importData.anniversaries)
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    data class ImportResult(
        val success: Boolean,
        val message: String,
        val data: ImportData? = null
    )
}