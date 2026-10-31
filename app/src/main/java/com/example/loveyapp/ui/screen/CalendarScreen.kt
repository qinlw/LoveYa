package com.example.loveyapp.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loveyapp.data.local.entity.CalendarMark
import com.example.loveyapp.ui.component.ToastPopup
import com.example.loveyapp.ui.viewmodel.CalendarViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/** 大姨妈标记的粉色 */
private val PeriodPink = Color(0xFFFF6B9D)

@Composable
fun CalendarScreen(
    padding: PaddingValues = PaddingValues(),
    viewModel: CalendarViewModel = hiltViewModel()
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var showPredictDialog by remember { mutableStateOf(false) }
    var showJumpDialog by remember { mutableStateOf(false) }
    var showDateJumpDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var episodeToDelete by remember { mutableStateOf<CalendarViewModel.PeriodEpisode?>(null) }

    val today = LocalDate.now()
    val month = viewModel.currentMonth

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
        // 顶部单层：日期居中，+ 与设置在右侧同一层；点击日期可跳转
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(start = 4.dp, end = 8.dp)
        ) {
            Text(
                text = "${month.year}年${month.monthValue}月",
                modifier = Modifier
                    .align(Alignment.Center)
                    .clickable { showJumpDialog = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "添加记录",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "设置",
                            tint = MaterialTheme.colorScheme.onSurface,
                            // 水平三点旋转 90°，作为设置入口
                            modifier = Modifier.size(30.dp).rotate(90f)
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("跳转到日期") },
                            onClick = {
                                showMenu = false
                                showDateJumpDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("搜索标记") },
                            onClick = {
                                showMenu = false
                                showSearchDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("日历推算") },
                            onClick = {
                                showMenu = false
                                showPredictDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("设置") },
                            onClick = {
                                showMenu = false
                                showSettingsDialog = true
                            }
                        )
                    }
                }
            }
        }

        // 月份切换带左右滑动过渡
        AnimatedContent(
            targetState = month,
            transitionSpec = {
                val forward = targetState.toEpochDay() > initialState.toEpochDay()
                val direction = if (forward) 1 else -1
                (slideInHorizontally(animationSpec = tween(260)) { width -> direction * width } +
                    fadeIn(animationSpec = tween(260))) togetherWith
                    (slideOutHorizontally(animationSpec = tween(260)) { width -> -direction * width } +
                        fadeOut(animationSpec = tween(260)))
            },
            label = "monthTransition"
        ) { targetMonth ->
            Column(modifier = Modifier.fillMaxWidth()) {
                // 星期表头（周一在前）
                val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                    weekDays.forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // 日期网格：固定行高，避免占满剩余空间导致下方留白
                val firstDayOfMonth = targetMonth.withDayOfMonth(1)
                // dayOfWeek: 1=周一 ... 7=周日，转为 0 起始偏移
                val offset = firstDayOfMonth.dayOfWeek.value - 1
                val daysInMonth = targetMonth.lengthOfMonth()
                val rowsNeeded = (offset + daysInMonth + 6) / 7

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        // 左右滑动翻月
                        .pointerInput(Unit) {
                            var totalDrag = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { totalDrag = 0f },
                                onDragCancel = { totalDrag = 0f },
                                onDragEnd = {
                                    val threshold = viewModel.swipeThresholdDp.dp.toPx()
                                    if (totalDrag <= -threshold) {
                                        viewModel.nextMonth()
                                    } else if (totalDrag >= threshold) {
                                        viewModel.prevMonth()
                                    }
                                    totalDrag = 0f
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    totalDrag += dragAmount
                                }
                            )
                        }
                ) {
                    repeat(rowsNeeded) { row ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            repeat(7) { column ->
                                val dayOfMonth = row * 7 + column - offset + 1
                                Box(
                                    modifier = Modifier.weight(1f).height(46.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (dayOfMonth in 1..daysInMonth) {
                                        val date = firstDayOfMonth.withDayOfMonth(dayOfMonth)
                                        val dayMarks = viewModel.marksForDate(date)
                                        DayCell(
                                            date = date,
                                            isToday = date == today,
                                            isSelected = selectedDate == date,
                                            hasPeriod = dayMarks.any { it.type == CalendarMark.TYPE_PERIOD },
                                            hasAnniversary = dayMarks.any { it.type == CalendarMark.TYPE_ANNIVERSARY },
                                            onClick = {
                                                // 点击仅选中该日期，不直接进入记录设置
                                                selectedDate = if (selectedDate == date) null else date
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 图例
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(12.dp).background(PeriodPink.copy(alpha = 0.35f), CircleShape))
                Text(" 大姨妈", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(5.dp).background(MaterialTheme.colorScheme.secondary, CircleShape))
                Text(" 纪念日", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }

        // 选中日期详情
        selectedDate?.let { date ->
            SelectedDayCard(
                date = date,
                marks = viewModel.marksForDate(date),
                onDelete = { viewModel.deleteMark(it) },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // 大姨妈周期信息
        PeriodSummaryCard(
            lastPeriod = viewModel.lastPeriodStart,
            averageCycle = viewModel.averageCycleDays,
            predictedNext = viewModel.predictedNextPeriod,
            episodes = viewModel.periodEpisodes(),
            today = today,
            onRecord = {
                // 日期自动取选中的日期（不能是未来）或今天
                val target = selectedDate?.takeIf { !it.isAfter(today) } ?: today
                viewModel.recordPeriodDay(target) { message -> toastMessage = message }
            },
            onDeleteEpisode = { episodeToDelete = it },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        }

        ToastPopup(
            message = toastMessage,
            onDismiss = { toastMessage = null }
        )
    }

    if (showJumpDialog) {
        MonthJumpDialog(
            currentMonth = month,
            today = today,
            onConfirm = { target ->
                viewModel.jumpToDate(target.withDayOfMonth(1))
                selectedDate = null
                showJumpDialog = false
            },
            onDismiss = { showJumpDialog = false }
        )
    }

    if (showDateJumpDialog) {
        SolarDatePickerDialog(
            initial = selectedDate ?: today,
            onConfirm = { date ->
                viewModel.jumpToDate(date)
                selectedDate = date
                showDateJumpDialog = false
            },
            onDismiss = { showDateJumpDialog = false }
        )
    }

    if (showSettingsDialog) {
        CalendarSettingsDialog(
            sensitivity = viewModel.swipeSensitivity,
            onSensitivityChange = { viewModel.updateSwipeSensitivity(it) },
            onDismiss = { showSettingsDialog = false }
        )
    }

    episodeToDelete?.let { episode ->
        AlertDialog(
            onDismissRequest = { episodeToDelete = null },
            title = { Text("删除经期记录") },
            text = {
                Text(
                    "确定删除「${episode.displayName}」（" +
                        episode.start.format(DateTimeFormatter.ofPattern("M月d日")) + " - " +
                        episode.end.format(DateTimeFormatter.ofPattern("M月d日")) +
                        "，共 ${episode.durationDays} 天）吗？"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePeriodEpisode(episode.start, episode.end)
                        episodeToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { episodeToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (showAddDialog) {
        val targetDate = selectedDate ?: today
        val hasPeriod = viewModel.marksForDate(targetDate).any { it.type == CalendarMark.TYPE_PERIOD }
        AddMarkDialog(
            date = targetDate,
            hasPeriod = hasPeriod,
            onTogglePeriod = { viewModel.togglePeriod(targetDate) },
            onAddAnniversary = { name -> viewModel.addAnniversaryMark(targetDate, name) },
            onDismiss = { showAddDialog = false }
        )
    }

    if (showSearchDialog) {
        SearchMarkDialog(
            viewModel = viewModel,
            onJump = { date ->
                viewModel.jumpToDate(date)
                selectedDate = date
                showSearchDialog = false
            },
            onDismiss = { showSearchDialog = false }
        )
    }

    if (showPredictDialog) {
        PredictDialog(
            lastPeriod = viewModel.lastPeriodStart,
            averageCycle = viewModel.averageCycleDays,
            predictedNext = viewModel.predictedNextPeriod,
            today = today,
            onDismiss = { showPredictDialog = false }
        )
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    hasPeriod: Boolean,
    hasAnniversary: Boolean,
    onClick: () -> Unit
) {
    val background = when {
        isSelected -> MaterialTheme.colorScheme.primary
        hasPeriod -> PeriodPink.copy(alpha = 0.35f)
        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        else -> Color.Transparent
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .background(background, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 14.sp,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )
        if (hasAnniversary) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp)
                    .size(5.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.secondary,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun SelectedDayCard(
    date: LocalDate,
    marks: List<CalendarMark>,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("yyyy年M月d日")),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (marks.isEmpty()) {
                Text(
                    text = "当天没有记录，点右上角 + 添加",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                marks.forEach { mark ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (mark.type == CalendarMark.TYPE_PERIOD) "大姨妈" else mark.name,
                            fontSize = 13.sp,
                            color = if (mark.type == CalendarMark.TYPE_PERIOD) PeriodPink else MaterialTheme.colorScheme.onSurface
                        )
                        TextButton(onClick = { onDelete(mark.id) }) {
                            Text("删除", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodSummaryCard(
    lastPeriod: LocalDate?,
    averageCycle: Int?,
    predictedNext: LocalDate?,
    episodes: List<CalendarViewModel.PeriodEpisode>,
    today: LocalDate,
    onRecord: () -> Unit,
    onDeleteEpisode: (CalendarViewModel.PeriodEpisode) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "大姨妈记录",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onRecord) {
                    Text("记录本次经期", fontSize = 12.sp, color = PeriodPink)
                }
            }

            if (lastPeriod == null) {
                Text(
                    text = "还没有记录，点右上角 + 记录",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 6.dp)
                )
            } else {
                val daysAgo = ChronoUnit.DAYS.between(lastPeriod, today)
                SummaryRow(
                    label = "上次开始",
                    value = lastPeriod.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))
                )
                SummaryRow(
                    label = "已经过去",
                    value = when {
                        daysAgo == 0L -> "今天"
                        daysAgo < 0L -> "尚未开始"
                        else -> "$daysAgo 天"
                    }
                )
                SummaryRow(
                    label = "平均周期",
                    value = averageCycle?.let { "$it 天" } ?: "记录不足 2 次"
                )
                if (predictedNext != null) {
                    val remain = ChronoUnit.DAYS.between(today, predictedNext)
                    SummaryRow(
                        label = "预计下次",
                        value = predictedNext.format(DateTimeFormatter.ofPattern("M月d日")) +
                            when {
                                remain > 0 -> "（还有 $remain 天）"
                                remain == 0L -> "（就是今天）"
                                else -> "（已过 ${-remain} 天）"
                            },
                        highlight = true
                    )
                } else {
                    SummaryRow(label = "预计下次", value = "记录不足 2 次")
                }
            }

            if (episodes.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(top = 10.dp))
                Text(
                    text = "经期记录（共 ${episodes.size} 次）",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
                episodes.takeLast(5).reversed().forEach { episode ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = episode.displayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PeriodPink
                            )
                            Text(
                                text = episode.start.format(DateTimeFormatter.ofPattern("M月d日")) +
                                    " - " +
                                    episode.end.format(DateTimeFormatter.ofPattern("M月d日")) +
                                    " · 持续 ${episode.durationDays} 天",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        TextButton(onClick = { onDeleteEpisode(episode) }) {
                            Text("删除", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) PeriodPink else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AddMarkDialog(
    date: LocalDate,
    hasPeriod: Boolean,
    onTogglePeriod: () -> Unit,
    onAddAnniversary: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var anniversaryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "添加记录 · ${date.monthValue}月${date.dayOfMonth}日",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("大姨妈", color = MaterialTheme.colorScheme.onSurface)
                    TextButton(onClick = { onTogglePeriod() }) {
                        Text(if (hasPeriod) "取消记录" else "记录", color = PeriodPink)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "纪念日标记",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = anniversaryName,
                    onValueChange = { anniversaryName = it },
                    label = { Text("纪念日名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                TextButton(
                    onClick = {
                        if (anniversaryName.isNotBlank()) {
                            onAddAnniversary(anniversaryName.trim())
                            anniversaryName = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("添加（每年重复）")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

@Composable
private fun SearchMarkDialog(
    viewModel: CalendarViewModel,
    onJump: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var keyword by remember { mutableStateOf("") }
    val results = viewModel.searchAnniversaries(keyword)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("搜索标记", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = keyword,
                    onValueChange = { keyword = it },
                    label = { Text("输入纪念日名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (keyword.isNotBlank() && results.isEmpty()) {
                    Text(
                        text = "没有找到相关标记",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
                results.take(20).forEach { mark ->
                    val markDate = runCatching { LocalDate.parse(mark.date) }.getOrNull()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { markDate?.let { onJump(it) } }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(mark.name, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = markDate?.format(DateTimeFormatter.ofPattern("yyyy年M月d日")) ?: mark.date,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    HorizontalDivider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
private fun PredictDialog(
    lastPeriod: LocalDate?,
    averageCycle: Int?,
    predictedNext: LocalDate?,
    today: LocalDate,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("日历推算", fontWeight = FontWeight.Bold) },
        text = {
            val formatter = DateTimeFormatter.ofPattern("yyyy年M月d日")
            if (lastPeriod == null) {
                Text("还没有大姨妈记录，先记录几次才能推算", color = MaterialTheme.colorScheme.onSurface)
            } else {
                Column {
                    Text(
                        text = "根据已记录的大姨妈日期自动推算。",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    SummaryRow("上次开始", lastPeriod.format(formatter))
                    SummaryRow(
                        "平均周期",
                        averageCycle?.let { "$it 天" } ?: "记录不足 2 次，暂无法计算"
                    )
                    if (predictedNext != null) {
                        SummaryRow("预计下次", predictedNext.format(formatter), highlight = true)
                        val remain = ChronoUnit.DAYS.between(today, predictedNext)
                        SummaryRow(
                            "距离今天",
                            if (remain >= 0) "还有 $remain 天" else "已过 ${-remain} 天"
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
private fun CalendarSettingsDialog(
    sensitivity: Int,
    onSensitivityChange: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var editing by remember { mutableStateOf(false) }
    var input by remember(sensitivity) { mutableStateOf(sensitivity.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("日历设置", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "左右滑动灵敏度",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (editing) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { text ->
                                if (text.length <= 3 && text.all { it.isDigit() }) input = text
                            },
                            modifier = Modifier.width(72.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        TextButton(
                            onClick = {
                                input.toIntOrNull()?.let { onSensitivityChange(it) }
                                editing = false
                            }
                        ) {
                            Text("确定")
                        }
                    } else {
                        Text(
                            text = sensitivity.toString(),
                            modifier = Modifier
                                .clickable { editing = true; input = sensitivity.toString() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Slider(
                    value = sensitivity.toFloat(),
                    onValueChange = { onSensitivityChange(it.roundToInt()) },
                    valueRange = 1f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "1 = 需要滑动较长距离，100 = 轻轻一划即可翻页；点击数字可直接输入",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("完成") }
        }
    )
}

@Composable
private fun MonthJumpDialog(
    currentMonth: LocalDate,
    today: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    var year by remember { mutableStateOf(currentMonth.year) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("跳转到月份", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { year-- }) { Text("‹ 上一年") }
                    Text("$year 年", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { year++ }) { Text("下一年 ›") }
                }
                repeat(4) { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(3) { column ->
                            val m = row * 3 + column + 1
                            val isCurrent =
                                year == currentMonth.year && m == currentMonth.monthValue
                            TextButton(
                                onClick = { onConfirm(LocalDate.of(year, m, 1)) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "${m}月",
                                    fontSize = 14.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
                TextButton(onClick = { onConfirm(today.withDayOfMonth(1)) }) {
                    Text("回到本月", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SolarDatePickerDialog(
    initial: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initial.atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onConfirm(
                            Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        )
                    }
                    onDismiss()
                }
            ) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    ) {
        DatePicker(state = state)
    }
}
