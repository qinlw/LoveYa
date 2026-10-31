package com.example.loveyapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveyapp.data.local.entity.CalendarMark
import com.example.loveyapp.data.prefs.DisplaySettingsStore
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
    private val calendarMarkRepository: CalendarMarkRepository,
    private val displaySettingsStore: DisplaySettingsStore
) : ViewModel() {
    var marks by mutableStateOf<List<CalendarMark>>(emptyList())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    /** 左右滑动翻月灵敏度（1~100，越大越灵敏） */
    var swipeSensitivity by mutableStateOf(displaySettingsStore.getCalendarSwipeSensitivity())
        private set

    /** 灵敏度换算成的滑动判定距离（dp）：1→60dp，100→8dp */
    val swipeThresholdDp: Int
        get() = 60 - (swipeSensitivity - 1) * 52 / 99

    fun updateSwipeSensitivity(value: Int) {
        val sensitivity = value.coerceIn(1, 100)
        swipeSensitivity = sensitivity
        displaySettingsStore.saveCalendarSwipeSensitivity(sensitivity)
    }

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

    /** 解析 "yyyy-MM-dd"（兼容带时间的旧数据） */
    private fun parseMarkDate(raw: String): LocalDate? =
        runCatching { LocalDate.parse(raw.trim().take(10)) }.getOrNull()

    /** 所有大姨妈记录日期（升序、去重） */
    fun allPeriodDates(): List<LocalDate> =
        marks.filter { it.type == CalendarMark.TYPE_PERIOD }
            .mapNotNull { parseMarkDate(it.date) }
            .distinct()
            .sorted()

    /**
     * 将日期列表按「是否相邻（相差 1 天）」聚类成一次次经期。
     * 是否连续只看前后一天有没有记录，因此补记中间缺失的日期后
     * （如已有 10.01、10.03，补记 10.02）会自动合并成一段。
     */
    private fun clusterDates(dates: List<LocalDate>): List<Pair<LocalDate, LocalDate>> {
        val sorted = dates.distinct().sorted()
        if (sorted.isEmpty()) return emptyList()
        val clusters = mutableListOf<Pair<LocalDate, LocalDate>>()
        var start = sorted.first()
        var end = start
        for (day in sorted.drop(1)) {
            if (ChronoUnit.DAYS.between(end, day) == 1L) {
                end = day
            } else {
                clusters.add(start to end)
                start = day
                end = day
            }
        }
        clusters.add(start to end)
        return clusters
    }

    private fun periodClusters(): List<Pair<LocalDate, LocalDate>> {
        val dates = allPeriodDates().filter { !it.isAfter(LocalDate.now()) }
        return clusterDates(dates)
    }

    /** 各次经期的开始日 */
    fun periodStarts(): List<LocalDate> = periodClusters().map { it.first }

    /** 一次完整的经期记录 */
    data class PeriodEpisode(
        val start: LocalDate,
        val end: LocalDate,
        /** 当年第几次，从 1 开始 */
        val indexInYear: Int
    ) {
        /** 持续天数（含开始和结束当天） */
        val durationDays: Long
            get() = ChronoUnit.DAYS.between(start, end) + 1

        val displayName: String
            get() = if (start.year == LocalDate.now().year) {
                "今年第 $indexInYear 次月经"
            } else {
                "${start.year} 年第 $indexInYear 次月经"
            }
    }

    /** 所有经期记录（按时间升序） */
    fun periodEpisodes(): List<PeriodEpisode> = episodesOf(periodClusters())

    private fun episodesOf(clusters: List<Pair<LocalDate, LocalDate>>): List<PeriodEpisode> {
        var year = Int.MIN_VALUE
        var index = 0
        return clusters.map { (start, end) ->
            if (start.year != year) {
                year = start.year
                index = 0
            }
            index++
            PeriodEpisode(start, end, index)
        }
    }

    /** 某天所在经期是本年第几次，用于记录完成后的提示 */
    fun episodeIndexFor(date: LocalDate): Int {
        val episodes = periodEpisodes()
        episodes.find { !date.isBefore(it.start) && !date.isAfter(it.end) }
            ?.let { return it.indexInYear }
        return episodes.count { it.start.year == date.year && it.start.isBefore(date) } + 1
    }

    /**
     * 记录某一天为大姨妈，日期由调用方给出（选中的日期或今天），
     * 不需要用户选择开始/结束日期。是否属于同一次经期由
     * 「前后一天是否有记录」自动判定，补记中间日期会自动合并。
     */
    fun recordPeriodDay(date: LocalDate, onResult: (String) -> Unit) {
        if (date.isAfter(LocalDate.now())) {
            onResult("不能记录未来日期")
            return
        }
        viewModelScope.launch {
            val alreadyMarked = marks.any {
                it.type == CalendarMark.TYPE_PERIOD && parseMarkDate(it.date) == date
            }
            if (alreadyMarked) {
                onResult("${date.monthValue}月${date.dayOfMonth}日已记录")
                return@launch
            }
            calendarMarkRepository.addMark(
                CalendarMark(date = date.format(dateFormatter), type = CalendarMark.TYPE_PERIOD, name = "大姨妈")
            )
            marks = calendarMarkRepository.getAllMarks()
            val index = episodeIndexFor(date)
            onResult("已记录 ${date.monthValue}月${date.dayOfMonth}日 · 今年第 $index 次")
        }
    }

    /** 删除一次经期的全部日期记录 */
    fun deletePeriodEpisode(start: LocalDate, end: LocalDate) {
        viewModelScope.launch {
            val startStr = start.format(dateFormatter)
            val endStr = end.format(dateFormatter)
            marks.filter {
                it.type == CalendarMark.TYPE_PERIOD &&
                    it.date.length >= 10 &&
                    it.date >= startStr && it.date <= endStr
            }.forEach { calendarMarkRepository.deleteMark(it.id) }
            loadMarks()
        }
    }

    /** 上次大姨妈开始日期（不含未来日期） */
    val lastPeriodStart: LocalDate?
        get() = periodStarts().lastOrNull()

    /** 平均周期天数：取最近 6 个周期，只要有 2 次记录即可显示 */
    val averageCycleDays: Int?
        get() {
            val starts = periodStarts()
            if (starts.size < 2) return null
            val intervals = starts.zipWithNext { a, b -> ChronoUnit.DAYS.between(a, b) }
                .filter { it > 0 }
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
