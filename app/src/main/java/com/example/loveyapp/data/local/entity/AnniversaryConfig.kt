package com.example.loveyapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "anniversary_config")
data class AnniversaryConfig(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    @SerializedName("name")
    val name: String,

    @ColumnInfo(name = "target_date")
    @SerializedName("targetDate")
    val targetDate: String,

    @ColumnInfo(name = "calendar_type")
    @SerializedName("calendarType")
    val calendarType: String = "SOLAR",

    @ColumnInfo(name = "display_order")
    @SerializedName("displayOrder")
    val displayOrder: Int = 0,

    @ColumnInfo(name = "enabled")
    @SerializedName("enabled")
    val enabled: Boolean = true,

    @ColumnInfo(name = "created_at")
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    @SerializedName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
)
