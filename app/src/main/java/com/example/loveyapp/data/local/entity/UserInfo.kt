package com.example.loveyapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_info")
data class UserInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "username") val username: String,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    @ColumnInfo(name = "user_birthday") val userBirthday: String,
    @ColumnInfo(name = "user_gender") val userGender: String,
    @ColumnInfo(name = "lover_name") val loverName: String,
    @ColumnInfo(name = "lover_birthday") val loverBirthday: String,
    @ColumnInfo(name = "lover_gender") val loverGender: String,
    @ColumnInfo(name = "anniversary_date") val anniversaryDate: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)