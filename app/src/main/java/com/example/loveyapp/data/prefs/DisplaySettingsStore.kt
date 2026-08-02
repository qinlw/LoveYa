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
    }

    fun getDisplayCalendarMode(): DisplayCalendarMode {
        return DisplayCalendarMode.fromValue(prefs.getString(KEY_DISPLAY_CALENDAR_MODE, null))
    }

    fun saveDisplayCalendarMode(mode: DisplayCalendarMode) {
        prefs.edit().putString(KEY_DISPLAY_CALENDAR_MODE, mode.value).apply()
    }
}
