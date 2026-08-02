package com.example.loveyapp.util

/**
 * 日期显示模式：控制首页纪念日、生日等日期的展示方式
 */
enum class DisplayCalendarMode(val value: String) {
    /** 只展示公历 */
    SOLAR_ONLY("SOLAR_ONLY"),
    /** 只展示农历 */
    LUNAR_ONLY("LUNAR_ONLY"),
    /** 公历+农历都展示 */
    BOTH("BOTH");

    companion object {
        fun fromValue(v: String?): DisplayCalendarMode {
            return values().firstOrNull { it.value == v } ?: BOTH
        }
    }
}
