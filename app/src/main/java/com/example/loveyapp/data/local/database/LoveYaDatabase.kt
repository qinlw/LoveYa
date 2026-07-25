package com.example.loveyapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.loveyapp.data.local.dao.AnniversaryConfigDao
import com.example.loveyapp.data.local.dao.DataBookDao
import com.example.loveyapp.data.local.dao.DiaryDao
import com.example.loveyapp.data.local.dao.UserInfoDao
import com.example.loveyapp.data.local.entity.AnniversaryConfig
import com.example.loveyapp.data.local.entity.DataBook
import com.example.loveyapp.data.local.entity.Diary
import com.example.loveyapp.data.local.entity.UserInfo

@Database(
    entities = [
        UserInfo::class,
        Diary::class,
        DataBook::class,
        AnniversaryConfig::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LoveYaDatabase : RoomDatabase() {
    abstract fun userInfoDao(): UserInfoDao
    abstract fun diaryDao(): DiaryDao
    abstract fun dataBookDao(): DataBookDao
    abstract fun anniversaryConfigDao(): AnniversaryConfigDao
}