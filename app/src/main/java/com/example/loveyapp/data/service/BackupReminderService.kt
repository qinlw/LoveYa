package com.example.loveyapp.data.service

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.example.loveyapp.security.KeyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupReminderService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyManager: KeyManager
) {
    private val prefs by lazy {
        keyManager.getEncryptedSharedPreferences("backup_prefs")
    }

    private val LAST_BACKUP_TIME_KEY = "last_backup_time"
    private val REMINDER_INTERVAL_KEY = "reminder_interval_days"
    private val DEFAULT_INTERVAL_DAYS = 7

    fun getLastBackupTime(): Long {
        return prefs.getLong(LAST_BACKUP_TIME_KEY, 0)
    }

    fun setLastBackupTime(time: Long) {
        prefs.edit().putLong(LAST_BACKUP_TIME_KEY, time).apply()
    }

    fun getReminderIntervalDays(): Int {
        return prefs.getInt(REMINDER_INTERVAL_KEY, DEFAULT_INTERVAL_DAYS)
    }

    fun setReminderIntervalDays(days: Int) {
        prefs.edit().putInt(REMINDER_INTERVAL_KEY, days).apply()
    }

    fun needsBackupReminder(): Boolean {
        val lastBackup = getLastBackupTime()
        if (lastBackup == 0L) {
            return true
        }

        val intervalMillis = getReminderIntervalDays().toLong() * 24 * 60 * 60 * 1000
        return System.currentTimeMillis() - lastBackup > intervalMillis
    }

    fun scheduleNextReminder() {
        val intervalMillis = getReminderIntervalDays().toLong() * 24 * 60 * 60 * 1000
        val workRequest: WorkRequest = OneTimeWorkRequestBuilder<BackupReminderWorker>()
            .setInitialDelay(intervalMillis, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun markBackupCompleted() {
        setLastBackupTime(System.currentTimeMillis())
        scheduleNextReminder()
    }
}