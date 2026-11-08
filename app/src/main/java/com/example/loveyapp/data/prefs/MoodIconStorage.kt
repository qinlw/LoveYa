package com.example.loveyapp.data.prefs

import android.content.Context
import android.net.Uri
import com.example.loveyapp.data.local.entity.CalendarMark
import java.io.File

/**
 * 用户自定义心情表情图的本地存储。
 * 每种心情对应一张图片，保存在应用私有目录 mood_icons/ 下，文件名固定，
 * 存在即表示该心情使用了自定义表情，删除即恢复默认。
 */
object MoodIconStorage {

    private const val DIR_NAME = "mood_icons"

    private fun fileNameFor(mood: String): String = when (mood) {
        CalendarMark.MOOD_HAPPY -> "happy.png"
        CalendarMark.MOOD_NORMAL -> "normal.png"
        CalendarMark.MOOD_SAD -> "sad.png"
        else -> "mood_" + mood.replace(Regex("[^a-zA-Z0-9_-]"), "") + ".png"
    }

    private fun iconDir(context: Context): File =
        File(context.filesDir, DIR_NAME).apply { if (!exists()) mkdirs() }

    /** 获取某种心情的自定义表情文件路径（文件可能不存在） */
    fun getCustomIconFile(context: Context, mood: String): File =
        File(iconDir(context), fileNameFor(mood))

    /** 是否为该心情设置了自定义表情 */
    fun hasCustomIcon(context: Context, mood: String): Boolean =
        getCustomIconFile(context, mood).exists()

    /**
     * 从选定的图片 URI 保存为该心情的自定义表情。
     * 复制内容到应用私有目录，避免依赖外部 URI 权限。
     */
    fun saveCustomIcon(context: Context, mood: String, uri: Uri): Boolean {
        return try {
            val target = getCustomIconFile(context, mood)
            context.contentResolver.openInputStream(uri)?.use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /** 删除某种心情的自定义表情，恢复使用默认图标 */
    fun resetCustomIcon(context: Context, mood: String) {
        runCatching { getCustomIconFile(context, mood).delete() }
    }
}
