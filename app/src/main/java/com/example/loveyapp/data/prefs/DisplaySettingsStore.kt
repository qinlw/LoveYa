package com.example.loveyapp.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.loveyapp.util.DisplayCalendarMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 应用显示偏好：日期展示模式等，非敏感信息使用普通 SharedPreferences。
 */
@Singleton
class DisplaySettingsStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("display_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_DISPLAY_CALENDAR_MODE = "display_calendar_mode"
        private const val KEY_CALENDAR_SWIPE_SENSITIVITY = "calendar_swipe_sensitivity"
        private const val KEY_CALENDAR_SWIPE_THRESHOLD = "calendar_swipe_threshold_dp"

        /** 滑动灵敏度默认值（1~100，越大越灵敏） */
        const val DEFAULT_SWIPE_SENSITIVITY = 70
    }

    fun getDisplayCalendarMode(): DisplayCalendarMode {
        return DisplayCalendarMode.fromValue(prefs.getString(KEY_DISPLAY_CALENDAR_MODE, null))
    }

    fun saveDisplayCalendarMode(mode: DisplayCalendarMode) {
        prefs.edit().putString(KEY_DISPLAY_CALENDAR_MODE, mode.value).apply()
    }

    /**
     * 日历左右滑动翻月的灵敏度（1~100，数值越大越灵敏）。
     * 兼容旧版的阈值（dp）：旧值 60dp→约 1，24dp→约 70，8dp→100。
     */
    fun getCalendarSwipeSensitivity(): Int {
        val saved = prefs.getInt(KEY_CALENDAR_SWIPE_SENSITIVITY, -1)
        if (saved in 1..100) return saved
        val legacyThreshold = prefs.getInt(KEY_CALENDAR_SWIPE_THRESHOLD, -1)
        if (legacyThreshold > 0) {
            val sensitivity = 1 + (60 - legacyThreshold.coerceIn(8, 60)) * 99 / 52
            return sensitivity.coerceIn(1, 100)
        }
        return DEFAULT_SWIPE_SENSITIVITY
    }

    fun saveCalendarSwipeSensitivity(sensitivity: Int) {
        prefs.edit().putInt(KEY_CALENDAR_SWIPE_SENSITIVITY, sensitivity.coerceIn(1, 100)).apply()
    }
}
