package com.example.loveyapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveyapp.data.local.entity.CalendarMark
import com.example.loveyapp.data.repository.CalendarMarkRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarMarkRepository: CalendarMarkRepository
) : ViewModel() {
    var marks by mutableStateOf<List<CalendarMark>>(emptyList())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    /** 当前展示的月份（取当月1号） */
    var currentMonth by mutableStateOf(LocalDate.now().withDayOfMonth(1))

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    init {
        loadMarks()
    }

    fun loadMarks() {
        viewModelScope.launch {
            isLoading = true
            error = null
            marks = calendarMarkRepository.getAllMarks()
            isLoading = false
        }
    }

    fun prevMonth() {
        currentMonth = currentMonth.minusMonths(1)
    }

    fun nextMonth() {
        currentMonth = currentMonth.plusMonths(1)
    }

    /** 某天的所有标记 */
    fun marksForDate(date: LocalDate): List<CalendarMark> {
        val dateStr = date.format(dateFormatter)
        return marks.filter { mark ->
            if (mark.type == CalendarMark.TYPE_ANNIVERSARY) {
                // 纪念日标记每年按公历月日重复
                mark.date.length >= 10 && mark.date.substring(5) == dateStr.substring(5)
            } else {
                mark.date == dateStr
            }
        }
    }

    /** 切换某天的大姨妈标记 */
    fun togglePeriod(date: LocalDate) {
        viewModelScope.launch {
            val dateStr = date.format(dateFormatter)
            val existing = marks.find { it.type == CalendarMark.TYPE_PERIOD && it.date == dateStr }
            if (existing != null) {
                calendarMarkRepository.deleteMark(existing.id)
            } else {
                calendarMarkRepository.addMark(
                    CalendarMark(date = dateStr, type = CalendarMark.TYPE_PERIOD, name = "大姨妈")
                )
            }
            loadMarks()
        }
    }

    fun addAnniversaryMark(date: LocalDate, name: String) {
        viewModelScope.launch {
            calendarMarkRepository.addMark(
                CalendarMark(
                    date = date.format(dateFormatter),
                    type = CalendarMark.TYPE_ANNIVERSARY,
                    name = name
                )
            )
            loadMarks()
        }
    }

    fun deleteMark(id: Long) {
        viewModelScope.launch {
            calendarMarkRepository.deleteMark(id)
            loadMarks()
        }
    }

    /** 所有大姨妈记录日期（升序） */
    fun allPeriodDates(): List<LocalDate> =
        marks.filter { it.type == CalendarMark.TYPE_PERIOD }
            .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
            .sorted()

    /** 各周期起始日：相邻记录间隔大于 10 天视为新周期开始 */
    fun periodStarts(): List<LocalDate> {
        val starts = mutableListOf<LocalDate>()
        var prev: LocalDate? = null
        for (day in allPeriodDates()) {
            if (prev == null || ChronoUnit.DAYS.between(prev, day) > 10) starts.add(day)
            prev = day
        }
        return starts
    }

    /** 上次大姨妈开始日期 */
    val lastPeriodStart: LocalDate?
        get() = periodStarts().lastOrNull()

    /** 平均周期天数：取最近 6 个周期，过滤 15~60 天的异常值 */
    val averageCycleDays: Int?
        get() {
            val starts = periodStarts()
            if (starts.size < 2) return null
            val intervals = starts.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b) }
                .filter { it in 15..60 }
                .takeLast(6)
            if (intervals.isEmpty()) return null
            return (intervals.sum().toDouble() / intervals.size).roundToInt()
        }

    /** 预计下次大姨妈日期 */
    val predictedNextPeriod: LocalDate?
        get() {
            val start = lastPeriodStart ?: return null
            val cycle = averageCycleDays ?: return null
            return start.plusDays(cycle.toLong())
        }

    /** 通过关键字搜索纪念日标记 */
    fun searchAnniversaries(keyword: String): List<CalendarMark> {
        if (keyword.isBlank()) return emptyList()
        return marks.filter {
            it.type == CalendarMark.TYPE_ANNIVERSARY && it.name.contains(keyword.trim(), ignoreCase = true)
        }.sortedBy { it.date }
    }

    /** 跳转到指定日期所在月份 */
    fun jumpToDate(date: LocalDate) {
        currentMonth = date.withDayOfMonth(1)
    }
}
