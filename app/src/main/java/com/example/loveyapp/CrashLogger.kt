package com.example.loveyapp

import android.content.Context
import android.os.Build
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

object CrashLogger {
    private const val LOG_DIR_NAME = "LoveYaLogs"
    private const val LOG_FILE_PREFIX = "crash_"
    private const val LOG_FILE_SUFFIX = ".txt"

    fun initialize(context: Context) {
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            val logFile = writeCrashLog(context, throwable)
            android.util.Log.e("LoveYaCrash", "Crash occurred, log saved to: ${logFile.absolutePath}")
            Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
        }
    }

    fun writeCrashLog(context: Context, throwable: Throwable): File {
        val logDir = getLogDirectory(context)
        if (!logDir.exists()) {
            logDir.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val logFile = File(logDir, "$LOG_FILE_PREFIX$timestamp$LOG_FILE_SUFFIX")

        try {
            FileWriter(logFile).use { writer ->
                writer.write("========== LOVE YA CRASH LOG ==========\n")
                writer.write("Timestamp: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
                writer.write("Android Version: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
                writer.write("Device: ${Build.MANUFACTURER} ${Build.MODEL}\n")
                writer.write("App Version: ${getAppVersion(context)}\n")
                writer.write("\n========== STACK TRACE ==========\n")

                val sw = StringWriter()
                throwable.printStackTrace(PrintWriter(sw))
                writer.write(sw.toString())

                writer.write("\n========== CAUSE ==========\n")
                throwable.cause?.let {
                    val causeSw = StringWriter()
                    it.printStackTrace(PrintWriter(causeSw))
                    writer.write(causeSw.toString())
                }

                writer.write("\n========== THREAD INFO ==========\n")
                writer.write("Thread: ${Thread.currentThread().name} (ID: ${Thread.currentThread().id})\n")
            }
        } catch (e: Exception) {
            android.util.Log.e("LoveYaCrash", "Failed to write crash log", e)
        }

        return logFile
    }

    fun getLogDirectory(context: Context): File {
        val externalDir = context.getExternalFilesDir(null)
        return if (externalDir != null) {
            File(externalDir, LOG_DIR_NAME)
        } else {
            File(context.filesDir, LOG_DIR_NAME)
        }
    }

    fun getCrashLogs(context: Context): List<File> {
        val logDir = getLogDirectory(context)
        return if (logDir.exists()) {
            logDir.listFiles { _, name -> name.startsWith(LOG_FILE_PREFIX) && name.endsWith(LOG_FILE_SUFFIX) }
                ?.sortedByDescending { it.lastModified() } ?: emptyList()
        } else {
            emptyList()
        }
    }

    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: Exception) {
            "unknown"
        }
    }
}
