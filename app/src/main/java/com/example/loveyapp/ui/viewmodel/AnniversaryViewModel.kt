package com.example.loveyapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveyapp.data.local.entity.AnniversaryConfig
import com.example.loveyapp.data.repository.AnniversaryRepository
import com.example.loveyapp.util.DisplayCalendarMode
import com.example.loveyapp.util.LunarCalendarUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class AnniversaryWithDays(
    val config: AnniversaryConfig,
    val daysRemaining: Int,
    val isExpired: Boolean
)

@HiltViewModel
class AnniversaryViewModel @Inject constructor(
    private val anniversaryRepository: AnniversaryRepository
) : ViewModel() {
    var anniversaries by mutableStateOf<List<AnniversaryWithDays>>(emptyList())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    init {
        loadAnniversaries()
    }

    fun loadAnniversaries() {
        viewModelScope.launch {
            isLoading = true
            error = null
            val configs = anniversaryRepository.getEnabledAnniversaries()
            anniversaries = configs.map { config ->
                val daysRemaining = calculateDaysRemaining(config.targetDate, config.calendarType, config.displayMode)
                AnniversaryWithDays(
                    config = config,
                    daysRemaining = daysRemaining,
                    isExpired = daysRemaining < 0
                )
            }.sortedBy { if (it.isExpired) Int.MAX_VALUE else it.daysRemaining }
            isLoading = false
        }
    }

    /**
     * 计算距离下次纪念日的天数。
     * - LUNAR_ONLY：按农历月日计算下次发生日期（把纪念日和今天都转成农历月日比较）
     * - SOLAR_ONLY / BOTH：按公历日期计算
     *
     * 注意：数据库 targetDate 始终存储公历 yyyy-MM-dd。
     */
    fun calculateDaysRemaining(targetDate: String, calendarTypeStr: String, displayModeStr: String): Int {
        return try {
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val targetSolar = LocalDate.parse(targetDate, formatter)
            val displayMode = DisplayCalendarMode.fromValue(displayModeStr)

            if (displayMode == DisplayCalendarMode.LUNAR_ONLY) {
                // LUNAR_ONLY 模式：按农历日期计算下次发生
                // 把纪念日 targetDate（公历）转成农历月日
                val targetLunar = LunarCalendarUtil.solarToLunar(
                    targetSolar.year, targetSolar.monthValue, targetSolar.dayOfMonth
                )
                val targetLunarMonth = targetLunar[1]
                val targetLunarDay = targetLunar[2]

                // 把今天转成农历月日
                val todayLunar = LunarCalendarUtil.solarToLunar(
                    today.year, today.monthValue, today.dayOfMonth
                )
                val todayLunarYear = todayLunar[0]
                val todayLunarMonth = todayLunar[1]
                val todayLunarDay = todayLunar[2]

                // 比较今天的农历月日和纪念日的农历月日
                val todayLunarMD = todayLunarMonth * 100 + todayLunarDay
                val targetLunarMD = targetLunarMonth * 100 + targetLunarDay

                // 若今天的农历月日 >= 纪念日农历月日，下次发生是明年，否则是今年
                val nextLunarYear = if (todayLunarMD >= targetLunarMD) todayLunarYear + 1 else todayLunarYear

                // 把下次发生的农历日期转回公历，算天数差
                val nextSolar = LunarCalendarUtil.lunarToSolar(nextLunarYear, targetLunarMonth, targetLunarDay)
                ChronoUnit.DAYS.between(today, nextSolar).toInt()
            } else {
                // SOLAR_ONLY 或 BOTH：用公历算
                var target = targetSolar
                var remaining = ChronoUnit.DAYS.between(today, target).toInt()
                if (remaining < 0) {
                    target = target.plusYears(1)
                    remaining = ChronoUnit.DAYS.between(today, target).toInt()
                }
                remaining
            }
        } catch (e: Exception) {
            0
        }
    }

    fun addAnniversary(name: String, targetDate: String, calendarType: String, displayMode: String, showYear: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val success = anniversaryRepository.addAnniversary(
                AnniversaryConfig(
                    name = name,
                    targetDate = targetDate,
                    calendarType = calendarType,
                    displayMode = displayMode,
                    displayOrder = anniversaries.size,
                    enabled = true,
                    showYear = showYear
                )
            ) != null
            if (success) {
                loadAnniversaries()
                onSuccess()
            } else {
                error = "添加失败"
            }
            isLoading = false
        }
    }

    fun updateAnniversary(id: Long, name: String, targetDate: String, calendarType: String, displayMode: String, showYear: Boolean, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val existing = anniversaryRepository.getAnniversaryById(id)
            if (existing != null) {
                val updated = existing.copy(
                    name = name,
                    targetDate = targetDate,
                    calendarType = calendarType,
                    displayMode = displayMode,
                    showYear = showYear,
                    updatedAt = System.currentTimeMillis()
                )
                val success = anniversaryRepository.updateAnniversary(updated)
                if (success) {
                    loadAnniversaries()
                    onSuccess()
                } else {
                    error = "更新失败"
                }
            } else {
                error = "纪念日不存在"
            }
            isLoading = false
        }
    }

    fun deleteAnniversary(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val success = anniversaryRepository.deleteAnniversary(id)
            if (success) {
                loadAnniversaries()
                onSuccess()
            } else {
                error = "删除失败"
            }
            isLoading = false
        }
    }
}
