package com.example.loveyapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * 日历页面标记：大姨妈记录 / 纪念日标记。
 * type: "PERIOD" = 大姨妈，"ANNIVERSARY" = 纪念日标记（每年按公历月日重复）
 */
@Entity(tableName = "calendar_mark")
data class CalendarMark(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long = 0,

    @ColumnInfo(name = "date")
    @SerializedName("date")
    val date: String,

    @ColumnInfo(name = "type")
    @SerializedName("type")
    val type: String,

    @ColumnInfo(name = "name")
    @SerializedName("name")
    val name: String = "",

    @ColumnInfo(name = "created_at")
    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    @SerializedName("updatedAt")
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_PERIOD = "PERIOD"
        const val TYPE_ANNIVERSARY = "ANNIVERSARY"
    }
}
