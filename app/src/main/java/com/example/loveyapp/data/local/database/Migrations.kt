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
