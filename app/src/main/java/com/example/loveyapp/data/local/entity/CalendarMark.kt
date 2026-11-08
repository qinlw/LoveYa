package com.example.loveyapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * 日历页面标记：大姨妈记录 / 纪念日标记 / 每日心情。
 * type: "PERIOD" = 大姨妈，"ANNIVERSARY" = 纪念日标记（每年按公历月日重复），
 * "MOOD" = 每日心情，name 为 HAPPY / NORMAL / SAD。
 * time 为 "HH:mm"，仅心情记录使用，一天可按不同时刻保存多条。
 * note 为心情记录的文字描述（记录原因），仅心情记录使用。
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

    @ColumnInfo(name = "time")
    @SerializedName("time")
    val time: String? = null,

    @ColumnInfo(name = "note")
    @SerializedName("note")
    val note: String? = null,

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
        const val TYPE_MOOD = "MOOD"

        const val MOOD_HAPPY = "HAPPY"
        const val MOOD_NORMAL = "NORMAL"
        const val MOOD_SAD = "SAD"
    }
}
