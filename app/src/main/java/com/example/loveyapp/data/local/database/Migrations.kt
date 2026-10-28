package com.example.loveyapp.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("DROP TABLE IF EXISTS notebook")
        database.execSQL("DROP TABLE IF EXISTS attribute")
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS data_book (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                content TEXT NOT NULL,
                created_at INTEGER NOT NULL DEFAULT 0,
                updated_at INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE anniversary_config ADD COLUMN calendar_type TEXT NOT NULL DEFAULT 'SOLAR'")
        database.execSQL("ALTER TABLE data_book ADD COLUMN tags TEXT NOT NULL DEFAULT ''")
    }
}

// 任务1（user_info 加 my_name）+ 任务3（anniversary_config 加 display_mode）合并迁移
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE user_info ADD COLUMN my_name TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE anniversary_config ADD COLUMN display_mode TEXT NOT NULL DEFAULT 'BOTH'")
    }
}

// anniversary_config 新增 show_year 字段，控制日期是否显示年份
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE anniversary_config ADD COLUMN show_year INTEGER NOT NULL DEFAULT 1")
    }
}

// diary/data_book 新增 is_pinned 置顶字段；新增 calendar_mark 表（日历页大姨妈/纪念日标记）
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE diary ADD COLUMN is_pinned INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE data_book ADD COLUMN is_pinned INTEGER NOT NULL DEFAULT 0")
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS calendar_mark (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                type TEXT NOT NULL,
                name TEXT NOT NULL DEFAULT '',
                created_at INTEGER NOT NULL DEFAULT 0,
                updated_at INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}

// anniversary_config 新增 is_pinned 置顶字段
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE anniversary_config ADD COLUMN is_pinned INTEGER NOT NULL DEFAULT 0")
    }
}
