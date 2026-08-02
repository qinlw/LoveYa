package com.example.loveyapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "user_info")
data class UserInfo(
    @PrimaryKey(autoGenerate = true)
    @SerializedName("id")
    val id: Long = 0,

    @ColumnInfo(name = "username")
    @SerializedName("username")
    val username: String,

    @ColumnInfo(name = "my_name")
    @SerializedName("myName")
    val myName: String = "",

    @ColumnInfo(name = "password_hash")
    @SerializedName("passwordHash")
    val passwordHash: String,

    @ColumnInfo(name = "user_birthday")
    @SerializedName("userBirthday")
    val userBirthday: String,

    @ColumnInfo(name = "user_gender")
    @SerializedName("userGender")
    val userGender: String,

    @ColumnInfo(name = "lover_name")
    @SerializedName("loverName")
    val loverName: String,

    @ColumnInfo(name = "lover_birthday")
    @SerializedName("loverBirthday")
    val loverBirthday: String,

    @ColumnInfo(name = "lover_gender")
    @SerializedName("loverGender")
    val loverGender: String,

    @ColumnInfo(name = "anniversary_date")
    @SerializedName("anniversaryDate")
    val anniversaryDate: String,

    @ColumnInfo(name = "created_at")
    @SerializedName("createdAt")
    val createdAt: Long
)
