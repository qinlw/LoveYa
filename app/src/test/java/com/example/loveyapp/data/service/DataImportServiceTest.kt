package com.example.loveyapp.data.service

import com.example.loveyapp.data.local.entity.AnniversaryConfig
import com.example.loveyapp.data.local.entity.DataBook
import com.example.loveyapp.data.local.entity.Diary
import com.example.loveyapp.data.local.entity.UserInfo
import com.google.gson.Gson
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class DataImportServiceTest {

    private lateinit var gson: Gson

    @Before
    fun setup() {
        gson = Gson()
    }

    @Test
    fun `ImportData should contain all required fields`() {
        val importData = DataImportService.ImportData(
            version = "1.0",
            exportDate = "2024-01-01T12:00:00",
            userInfo = null,
            diaries = emptyList(),
            dataBooks = emptyList(),
            anniversaries = emptyList()
        )

        assertEquals("1.0", importData.version)
        assertEquals("2024-01-01T12:00:00", importData.exportDate)
        assertNull(importData.userInfo)
        assertTrue(importData.diaries.isEmpty())
        assertTrue(importData.dataBooks.isEmpty())
        assertTrue(importData.anniversaries.isEmpty())
    }

    @Test
    fun `ImportData deserialization should work correctly`() {
        val json = """
            {
                "version": "1.0",
                "exportDate": "2024-01-01T12:00:00",
                "userInfo": {
                    "id": 1,
                    "username": "testuser",
                    "passwordHash": "hash",
                    "userBirthday": "1990-01-01",
                    "userGender": "男",
                    "loverName": "爱人",
                    "loverBirthday": "1991-01-01",
                    "loverGender": "女",
                    "anniversaryDate": "2020-01-01",
                    "createdAt": 1234567890000
                },
                "diaries": [
                    {
                        "id": 1,
                        "notebookName": "日记本",
                        "date": "2024-01-01",
                        "weather": "晴天",
                        "content": "今天天气很好",
                        "tags": "开心"
                    }
                ],
                "dataBooks": [
                    {
                        "id": 1,
                        "notebookName": "资料本",
                        "attributeName": "身高",
                        "attributeValues": "180cm"
                    }
                ],
                "anniversaries": [
                    {
                        "id": 1,
                        "name": "恋爱纪念日",
                        "targetDate": "2020-01-01",
                        "displayOrder": 0,
                        "enabled": true
                    }
                ]
            }
        """.trimIndent()

        val importData = gson.fromJson(json, DataImportService.ImportData::class.java)

        assertEquals("1.0", importData.version)
        assertEquals("2024-01-01T12:00:00", importData.exportDate)
        assertNotNull(importData.userInfo)
        assertEquals("testuser", importData.userInfo?.username)
        assertEquals(1, importData.diaries.size)
        assertEquals("日记本", importData.diaries[0].notebookName)
        assertEquals(1, importData.dataBooks.size)
        assertEquals("资料本", importData.dataBooks[0].notebookName)
        assertEquals(1, importData.anniversaries.size)
        assertEquals("恋爱纪念日", importData.anniversaries[0].name)
    }

    @Test
    fun `ImportData with null userInfo should be deserialized correctly`() {
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

        val importData = gson.fromJson(json, DataImportService.ImportData::class.java)

        assertNull(importData.userInfo)
    }

    @Test
    fun `ImportResult should handle success case`() {
        val importData = DataImportService.ImportData(
            version = "1.0",
            exportDate = "2024-01-01T12:00:00",
            userInfo = null,
            diaries = emptyList(),
            dataBooks = emptyList(),
            anniversaries = emptyList()
        )

        val result = DataImportService.ImportResult(
            success = true,
            message = "解析成功",
            data = importData
        )

        assertTrue(result.success)
        assertEquals("解析成功", result.message)
        assertNotNull(result.data)
    }

    @Test
    fun `ImportResult should handle failure case without data`() {
        val result = DataImportService.ImportResult(
            success = false,
            message = "解析失败"
        )

        assertFalse(result.success)
        assertEquals("解析失败", result.message)
        assertNull(result.data)
    }

    @Test
    fun `ImportData serialization should produce valid JSON`() {
        val importData = DataImportService.ImportData(
            version = "1.0",
            exportDate = "2024-01-01T12:00:00",
            userInfo = null,
            diaries = emptyList(),
            dataBooks = emptyList(),
            anniversaries = emptyList()
        )

        val json = gson.toJson(importData)

        assertTrue(json.contains("\"version\":\"1.0\""))
        assertTrue(json.contains("\"exportDate\":\"2024-01-01T12:00:00\""))
    }

    @Test
    fun `ImportData should handle empty collections`() {
        val importData = DataImportService.ImportData(
            version = "1.0",
            exportDate = "2024-01-01T12:00:00",
            userInfo = null,
            diaries = emptyList(),
            dataBooks = emptyList(),
            anniversaries = emptyList()
        )

        assertTrue(importData.diaries.isEmpty())
        assertTrue(importData.dataBooks.isEmpty())
        assertTrue(importData.anniversaries.isEmpty())
    }
}