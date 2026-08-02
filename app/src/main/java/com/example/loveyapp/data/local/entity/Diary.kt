package com.example.loveyapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "diary")
data class Diary(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long = 0,

    @ColumnInfo(name = "notebook_name")
    @SerializedName("notebookName")
    val notebookName: String,

    @ColumnInfo(name = "content")
    @SerializedName("content")
    val content: String,

    @ColumnInfo(name = "date")
    @SerializedName("date")
    val date: String,

    @ColumnInfo(name = "tags")
    @SerializedName("tags")
    val tags: String,

    @ColumnInfo(name = "created_at")
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    @SerializedName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
)
