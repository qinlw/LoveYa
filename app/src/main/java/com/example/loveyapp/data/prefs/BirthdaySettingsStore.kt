package com.example.loveyapp.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.example.loveyapp.security.AuthService
import com.example.loveyapp.util.CalendarType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 生日日历类型偏好存储。
 *
 * 按当前登录用户隔离存储（`birthday_settings_{username}`），
 * "我的生日"和"爱人生日"分别独立保存，不影响全局显示设置。
 */
@Singleton
class BirthdaySettingsStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authService: AuthService
) {
    /** 缓存已创建的 SharedPreferences 实例，避免重复创建。 */
    private val prefsCache = mutableMapOf<String, SharedPreferences>()

    private fun prefs(): SharedPreferences {
        val username = authService.currentUsername
            ?: throw IllegalStateException("未登录，无法访问生日设置")
        return prefsCache.getOrPut(username) {
            context.getSharedPreferences("birthday_settings_$username", Context.MODE_PRIVATE)
        }
    }

    /** 获取"我的生日"使用的日历类型，默认公历 */
    fun getMyBirthdayCalendarType(): CalendarType {
        val value = try { prefs().getString(KEY_MY_BIRTHDAY_CALENDAR, null) } catch (_: Exception) { null }
        return if (value == CalendarType.LUNAR.name) CalendarType.LUNAR else CalendarType.SOLAR
    }

    /** 保存"我的生日"使用的日历类型 */
    fun saveMyBirthdayCalendarType(type: CalendarType) {
        try { prefs().edit().putString(KEY_MY_BIRTHDAY_CALENDAR, type.name).apply() } catch (_: Exception) {}
    }

    /** 获取"爱人生日"使用的日历类型，默认公历 */
    fun getLoverBirthdayCalendarType(): CalendarType {
        val value = try { prefs().getString(KEY_LOVER_BIRTHDAY_CALENDAR, null) } catch (_: Exception) { null }
        return if (value == CalendarType.LUNAR.name) CalendarType.LUNAR else CalendarType.SOLAR
    }

    /** 保存"爱人生日"使用的日历类型 */
    fun saveLoverBirthdayCalendarType(type: CalendarType) {
        try { prefs().edit().putString(KEY_LOVER_BIRTHDAY_CALENDAR, type.name).apply() } catch (_: Exception) {}
    }

    /** 切换"我的生日"日历类型（公历 ↔ 农历），返回切换后的新值 */
    fun toggleMyBirthdayCalendarType(): CalendarType {
        val current = getMyBirthdayCalendarType()
        val newType = if (current == CalendarType.SOLAR) CalendarType.LUNAR else CalendarType.SOLAR
        saveMyBirthdayCalendarType(newType)
        return newType
    }

    /** 切换"爱人生日"日历类型（公历 ↔ 农历），返回切换后的新值 */
    fun toggleLoverBirthdayCalendarType(): CalendarType {
        val current = getLoverBirthdayCalendarType()
        val newType = if (current == CalendarType.SOLAR) CalendarType.LUNAR else CalendarType.SOLAR
        saveLoverBirthdayCalendarType(newType)
        return newType
    }

    /** 获取"我们的开始"使用的日历类型，默认公历 */
    fun getAnniversaryCalendarType(): CalendarType {
        val value = try { prefs().getString(KEY_ANNIVERSARY_CALENDAR, null) } catch (_: Exception) { null }
        return if (value == CalendarType.LUNAR.name) CalendarType.LUNAR else CalendarType.SOLAR
    }

    /** 保存"我们的开始"使用的日历类型 */
    fun saveAnniversaryCalendarType(type: CalendarType) {
        try { prefs().edit().putString(KEY_ANNIVERSARY_CALENDAR, type.name).apply() } catch (_: Exception) {}
    }

    companion object {
        private const val KEY_MY_BIRTHDAY_CALENDAR = "my_birthday_calendar_type"
        private const val KEY_LOVER_BIRTHDAY_CALENDAR = "lover_birthday_calendar_type"
        private const val KEY_ANNIVERSARY_CALENDAR = "anniversary_calendar_type"
    }
}
