package com.example.loveyapp.data.service

import com.example.loveyapp.data.local.entity.AnniversaryConfig
import com.example.loveyapp.data.local.entity.DataBook
import com.example.loveyapp.data.local.entity.Diary
import com.example.loveyapp.data.local.entity.UserInfo
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DataExportServiceTest {

    private lateinit var gson: Gson

    @Before
    fun setup() {
        gson = Gson()
    }

    @Test
    fun `ExportData should have correct version`() {
        val exportData = DataExportService.ExportData(
            exportDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            userInfo = null,
            diaries = emptyList(),
            dataBooks = emptyList(),
            anniversaries = emptyList()
        )

        assertEquals("1.0", exportData.version)
    }

    @Test
    fun `ExportData should contain all fields`() {
        val userInfo = UserInfo(
            id = 1,
            username = "testuser",
            passwordHash = "hash",
            userBirthday = "1990-01-01",
            userGender = "男",
            loverName = "爱人",
            loverBirthday = "1991-01-01",
            loverGender = "女",
            anniversaryDate = "2020-01-01",
            createdAt = System.currentTimeMillis()
        )

        val diary = Diary(
            id = 1,
            notebookName = "日记本",
            content = "今天天气很好",
            date = "2024-01-01",
            tags = "开心"
        )

        val dataBook = DataBook(
            id = 1,
            notebookName = "资料本",
            attributeName = "身高",
            attributeValues = "180cm"
        )

        val anniversary = AnniversaryConfig(
            id = 1,
            name = "恋爱纪念日",
            targetDate = "2020-01-01",
            displayOrder = 0,
            enabled = true
        )

        val exportData = DataExportService.ExportData(
            exportDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            userInfo = userInfo,
            diaries = listOf(diary),
            dataBooks = listOf(dataBook),
            anniversaries = listOf(anniversary)
        )

        assertNotNull(exportData.userInfo)
        assertEquals(1, exportData.diaries.size)
        assertEquals(1, exportData.dataBooks.size)
        assertEquals(1, exportData.anniversaries.size)
        assertEquals("testuser", exportData.userInfo?.username)
        assertEquals("日记本", exportData.diaries[0].notebookName)
        assertEquals("资料本", exportData.dataBooks[0].notebookName)
        assertEquals("恋爱纪念日", exportData.anniversaries[0].name)
    }

    @Test
    fun `ExportData serialization should produce valid JSON`() {
        val exportData = DataExportService.ExportData(
            exportDate = "2024-01-01T12:00:00",
            userInfo = null,
            diaries = emptyList(),
            dataBooks = emptyList(),
            anniversaries = emptyList()
        )

        val json = gson.toJson(exportData)

        assertTrue(json.contains("version"))
        assertTrue(json.contains("1.0"))
        assertTrue(json.contains("exportDate"))
        assertTrue(json.contains("2024-01-01T12:00:00"))
        assertTrue(json.contains("diaries"))
        assertTrue(json.contains("dataBooks"))
        assertTrue(json.contains("anniversaries"))
    }

    @Test
    fun `ExportData deserialization should work correctly`() {
        val json = """
            {
                "version": "1.0",
                "exportDate": "2024-01-01T12:00:00",
                "userInfo": null,
                "diaries": [],
                "dataBooks": [],
                "anniversaries": []
            }
        """.trimIndent()

        val exportData = gson.fromJson(json, DataExportService.ExportData::class.java)

        assertEquals("1.0", exportData.version)
        assertEquals("2024-01-01T12:00:00", exportData.exportDate)
        assertNull(exportData.userInfo)
        assertTrue(exportData.diaries.isEmpty())
        assertTrue(exportData.dataBooks.isEmpty())
        assertTrue(exportData.anniversaries.isEmpty())
    }

    @Test
    fun `ExportData should handle empty collections`() {
        val exportData = DataExportService.ExportData(
            exportDate = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            userInfo = null,
            diaries = emptyList(),
            dataBooks = emptyList(),
            anniversaries = emptyList()
        )

        assertTrue(exportData.diaries.isEmpty())
        assertTrue(exportData.dataBooks.isEmpty())
        assertTrue(exportData.anniversaries.isEmpty())
    }

    @Test
    fun `ExportData with userInfo should serialize correctly`() {
        val userInfo = UserInfo(
            id = 1,
            username = "testuser",
            passwordHash = "hash",
            userBirthday = "1990-01-01",
            userGender = "男",
            loverName = "爱人",
            loverBirthday = "1991-01-01",
            loverGender = "女",
            anniversaryDate = "2020-01-01",
            createdAt = 1234567890000L
        )

        val exportData = DataExportService.ExportData(
            exportDate = "2024-01-01T12:00:00",
            userInfo = userInfo,
            diaries = emptyList(),
            dataBooks = emptyList(),
            anniversaries = emptyList()
        )

        val json = gson.toJson(exportData)
        assertTrue(json.contains("\"username\":\"testuser\""))
        assertTrue(json.contains("\"loverName\":\"爱人\""))
        assertTrue(json.contains("\"anniversaryDate\":\"2020-01-01\""))
    }
}