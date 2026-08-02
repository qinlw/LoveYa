package com.example.loveyapp.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.loveyapp.util.LunarCalendarUtil

/**
 * 农历日期选择器对话框。
 * 选择方式：年/月/日 三列滚动，确认后回调农历文字（如"丙午马年 八月 十五"）和公历 LocalDate。
 *
 * @param initialSolarDate 初始公历日期（yyyy-MM-dd），用于回填农历选择器
 * @param onConfirm 回调：农历显示文字、公历 yyyy-MM-dd 字符串
 */
@Composable
fun LunarDatePickerDialog(
    initialSolarDate: String,
    onConfirm: (lunarDisplay: String, solarFormatted: String) -> Unit,
    onDismiss: () -> Unit
) {
    // 解析初始公历 → 农历序号
    val initialLunar = remember(initialSolarDate) {
        try {
            val parts = initialSolarDate.split("-")
            if (parts.size == 3) {
                val y = parts[0].toInt()
                val m = parts[1].toInt()
                val d = parts[2].toInt()
                LunarCalendarUtil.solarToLunar(y, m, d) // [year, month, day, isLeap]
            } else null
        } catch (_: Exception) {
            null
        }
    }
    val today = java.time.LocalDate.now()
    val todayLunar = remember { LunarCalendarUtil.solarToLunar(today.year, today.monthValue, today.dayOfMonth) }

    val initYear = initialLunar?.get(0) ?: todayLunar[0]
    val initMonthOrder = remember(initialLunar, initYear) {
        var m = initialLunar?.get(1) ?: todayLunar[1]
        val leap = LunarCalendarUtil.getLeapMonthOfYear(initYear)
        if (leap != 0 && initialLunar?.get(3) == 1 && m == leap) {
            m = leap + 1 // 闰月 → 序号
        } else if (leap != 0 && m > leap && initialLunar?.get(3) != 1) {
            m += 1 // 闰月之后的正常月，序号 +1
        }
        m.coerceIn(1, LunarCalendarUtil.getLunarMonthNamesInYear(initYear).size)
    }
    val initDay = initialLunar?.get(2) ?: todayLunar[2]

    var selectedYear by remember { mutableIntStateOf(initYear) }
    var selectedMonthOrder by remember { mutableIntStateOf(initMonthOrder) }
    var selectedDay by remember { mutableIntStateOf(initDay) }

    val minYear = 1900
    val maxYear = 2099
    val years = remember { (minYear..maxYear).toList() }
    val yearNames = remember { years.map { "${it}年（${LunarCalendarUtil.getLunarYearDisplayName(it)}）" } }

    val monthNames = remember(selectedYear) {
        LunarCalendarUtil.getLunarMonthNamesInYear(selectedYear)
    }
    val dayNames = remember(selectedYear, selectedMonthOrder) {
        LunarCalendarUtil.getLunarDayNamesInMonth(selectedYear, selectedMonthOrder)
    }

    // 月份/日期变化时校验越界
    LaunchedEffect(selectedYear) {
        if (selectedMonthOrder > monthNames.size) {
            selectedMonthOrder = monthNames.size
        }
    }
    LaunchedEffect(selectedYear, selectedMonthOrder) {
        if (selectedDay > dayNames.size) {
            selectedDay = dayNames.size
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "选择农历日期",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WheelColumn(
                        items = yearNames,
                        selectedIndex = years.indexOf(selectedYear),
                        modifier = Modifier.weight(1.4f),
                        onSelected = { idx ->
                            selectedYear = years[idx]
                        }
                    )
                    WheelColumn(
                        items = monthNames,
                        selectedIndex = selectedMonthOrder - 1,
                        modifier = Modifier.weight(1f),
                        onSelected = { idx ->
                            selectedMonthOrder = idx + 1
                        }
                    )
                    WheelColumn(
                        items = dayNames,
                        selectedIndex = selectedDay - 1,
                        modifier = Modifier.weight(1f),
                        onSelected = { idx ->
                            selectedDay = idx + 1
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 预览
                val previewLunar = "${LunarCalendarUtil.getLunarYearDisplayName(selectedYear)} ${monthNames[selectedMonthOrder - 1]} ${dayNames[selectedDay - 1]}"
                val previewSolar = LunarCalendarUtil.lunarOrderToSolar(selectedYear, selectedMonthOrder, selectedDay)
                Text(
                    text = previewLunar,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "对应公历：${previewSolar}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        val solar = LunarCalendarUtil.lunarOrderToSolar(selectedYear, selectedMonthOrder, selectedDay)
                        val solarStr = solar.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        onConfirm(previewLunar, solarStr)
                    }) {
                        Text("确认")
                    }
                }
            }
        }
    }
}

/**
 * 简易滚动选择列。点击条目选中，中间高亮，自动滚动到选中项附近。
 */
@Composable
private fun WheelColumn(
    items: List<String>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    onSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0 && items.isNotEmpty()) {
            // 滚动到选中项附近（让选中项尽量居中可见）
            val target = (selectedIndex - 2).coerceAtLeast(0)
            listState.scrollToItem(target)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        itemsIndexed(items) { index, item ->
            val isSelected = index == selectedIndex
            val color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
            }
            val weight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            val size = if (isSelected) 16.sp else 14.sp
            Text(
                text = item,
                color = color,
                fontWeight = weight,
                fontSize = size,
                modifier = Modifier
                    .clickable { onSelected(index) }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}
