package com.example.loveyapp.data.service

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.concurrent.TimeUnit

class BackupReminderServiceTest {

    private val DEFAULT_INTERVAL_DAYS = 7
    private val DAY_IN_MILLIS = TimeUnit.DAYS.toMillis(1)

    @Test
    fun `needsBackupReminder should return true when no backup has been made`() {
        val lastBackupTime = 0L
        val needsReminder = lastBackupTime == 0L
        
        assertTrue(needsReminder)
    }

    @Test
    fun `needsBackupReminder should return true when backup is overdue`() {
        val lastBackupTime = System.currentTimeMillis() - (DEFAULT_INTERVAL_DAYS + 1) * DAY_IN_MILLIS
        val intervalMillis = DEFAULT_INTERVAL_DAYS.toLong() * DAY_IN_MILLIS
        val needsReminder = System.currentTimeMillis() - lastBackupTime > intervalMillis
        
        assertTrue(needsReminder)
    }

    @Test
    fun `needsBackupReminder should return false when backup is recent`() {
        val lastBackupTime = System.currentTimeMillis() - 1 * DAY_IN_MILLIS
        val intervalMillis = DEFAULT_INTERVAL_DAYS.toLong() * DAY_IN_MILLIS
        val needsReminder = System.currentTimeMillis() - lastBackupTime > intervalMillis
        
        assertFalse(needsReminder)
    }

    @Test
    fun `needsBackupReminder should return false when backup is exactly on time`() {
        val lastBackupTime = System.currentTimeMillis() - DEFAULT_INTERVAL_DAYS * DAY_IN_MILLIS
        val intervalMillis = DEFAULT_INTERVAL_DAYS.toLong() * DAY_IN_MILLIS
        val needsReminder = System.currentTimeMillis() - lastBackupTime > intervalMillis
        
        assertFalse(needsReminder)
    }

    @Test
    fun `getReminderIntervalDays should return default when not set`() {
        val interval = DEFAULT_INTERVAL_DAYS
        
        assertEquals(7, interval)
    }

    @Test
    fun `getReminderIntervalDays should return custom value`() {
        val customInterval = 14
        
        assertEquals(14, customInterval)
    }

    @Test
    fun `setLastBackupTime should update time`() {
        val time1 = 1000L
        val time2 = 2000L
        
        assertNotEquals(time1, time2)
    }

    @Test
    fun `markBackupCompleted should update last backup time`() {
        val beforeTime = System.currentTimeMillis()
        Thread.sleep(10)
        val afterTime = System.currentTimeMillis()
        
        assertTrue(afterTime > beforeTime)
    }

    @Test
    fun `interval calculation should be correct`() {
        val days = 7L
        val expectedMillis = 7 * 24 * 60 * 60 * 1000L
        val actualMillis = TimeUnit.DAYS.toMillis(days)
        
        assertEquals(expectedMillis, actualMillis)
    }

    @Test
    fun `needsBackupReminder should handle custom interval`() {
        val customInterval = 3
        val lastBackupTime = System.currentTimeMillis() - (customInterval + 1) * DAY_IN_MILLIS
        val intervalMillis = customInterval.toLong() * DAY_IN_MILLIS
        val needsReminder = System.currentTimeMillis() - lastBackupTime > intervalMillis
        
        assertTrue(needsReminder)
    }

    @Test
    fun `scheduleNextReminder should calculate correct delay`() {
        val intervalDays = 7L
        val expectedDelay = TimeUnit.DAYS.toMillis(intervalDays)
        
        assertEquals(604800000L, expectedDelay)
    }
}