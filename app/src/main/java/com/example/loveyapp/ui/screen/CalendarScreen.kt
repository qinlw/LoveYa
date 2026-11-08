package com.example.loveyapp.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import com.example.loveyapp.data.prefs.MoodEntryStore
import com.example.loveyapp.data.prefs.MoodIconStorage
import com.example.loveyapp.data.prefs.MoodType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loveyapp.R
import com.example.loveyapp.data.local.entity.CalendarMark
import com.example.loveyapp.data.local.entity.Diary
import com.example.loveyapp.ui.component.ToastPopup
import com.example.loveyapp.ui.viewmodel.CalendarViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

/** 大姨妈标记的粉色 */
private val PeriodPink = Color(0xFFFF6B9D)

/** 内置心情对应的默认表情；自定义心情使用通用默认表情 */
private fun moodDefaultRes(moodId: String): Int = when (moodId) {
    CalendarMark.MOOD_HAPPY -> R.drawable.ic_mood_happy
    CalendarMark.MOOD_NORMAL -> R.drawable.ic_mood_normal
    CalendarMark.MOOD_SAD -> R.drawable.ic_mood_sad
    else -> R.drawable.ic_mood_custom
}

/**
 * 心情图标：优先使用用户在设置中自定义的图片，否则使用内置表情。
 */
@Composable
private fun moodIconPainter(mood: String): Painter {
    val context = LocalContext.current
    val customFile = MoodIconStorage.getCustomIconFile(context, mood)
    val hasCustom = customFile.exists()
    return if (hasCustom) {
        val bitmap = remember(customFile.absolutePath, customFile.lastModified()) {
            runCatching { BitmapFactory.decodeFile(customFile.absolutePath)?.asImageBitmap() }.getOrNull()
        }
        if (bitmap != null) remember(bitmap) { BitmapPainter(bitmap) }
        else painterResource(id = moodDefaultRes(mood))
    } else {
        painterResource(id = moodDefaultRes(mood))
    }
}

/**
 * 左上角圆形「心情一览」入口使用的图片：优先用户自定义图片，默认使用开心表情。
 * version 变化时重新读取文件，保证设置中更换/恢复后立即刷新。
 */
@Composable
private fun moodEntryPainter(version: Int): Painter {
    val context = LocalContext.current
    val file = MoodEntryStore.getIconFile(context)
    return if (file.exists()) {
        val bitmap = remember(file.absolutePath, file.lastModified(), version) {
            runCatching { BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap() }.getOrNull()
        }
        if (bitmap != null) remember(bitmap) { BitmapPainter(bitmap) }
        else painterResource(id = R.drawable.ic_mood_happy)
    } else {
        painterResource(id = R.drawable.ic_mood_happy)
    }
}

/**
 * 圆形心情入口按钮。开启拖动时显示浅色描边，提示用户可拖动。
 */
@Composable
private fun MoodEntryButton(
    modifier: Modifier = Modifier,
    iconVersion: Int,
    draggable: Boolean,
    sizeDp: Int
) {
    Surface(
        modifier = modifier
            .size(sizeDp.dp)
            .then(
                if (draggable) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        shape = CircleShape
                    )
                } else {
                    Modifier
                }
            ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = if (draggable) 8.dp else 3.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = moodEntryPainter(iconVersion),
                contentDescription = "心情一览",
                modifier = Modifier.size((sizeDp * 0.6f).dp)
            )
        }
    }
}

@Composable
fun CalendarScreen(
    padding: PaddingValues = PaddingValues(),
    onOpenDiary: (Long) -> Unit = {},
    onWriteDiary: (LocalDate) -> Unit = {},
    onOpenMoods: () -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel()
) {
    // 每次进入日历页重新加载标记与日记，保证从日记页返回后数据是最新的
    LaunchedEffect(Unit) {
        viewModel.loadMarks()
        viewModel.loadDiaries()
        viewModel.reloadMoodTypes()
    }
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

    // 「心情一览」入口配置：圆形按钮（位置/大小/贴边/拖动）+ 旧版横条入口开关
    val entryContext = LocalContext.current
    var entryDragEnabled by remember { mutableStateOf(MoodEntryStore.isDragEnabled(entryContext)) }
    var entryIconVersion by remember { mutableStateOf(0) }
    var entrySizeDp by remember { mutableStateOf(MoodEntryStore.getButtonSizeDp(entryContext)) }
    var entryMarginDp by remember { mutableStateOf(MoodEntryStore.getEdgeMarginDp(entryContext)) }
    var showLegacyEntry by remember { mutableStateOf(MoodEntryStore.isLegacyEntryVisible(entryContext)) }
    // 恢复默认布局后递增，用于强制重新计算位置
    var entryLayoutVersion by remember { mutableStateOf(0) }
    // 全屏设置面板的透明度
    var settingsTransparency by remember {
        mutableStateOf(MoodEntryStore.getSettingsTransparency(entryContext))
    }

    val today = LocalDate.now()
    val month = viewModel.currentMonth

    // 根容器：设置面板作为覆盖层叠在日历之上，便于调节透明度时看到日历实时变化
    Box(modifier = Modifier.fillMaxSize()) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
    ) {
        val density = LocalDensity.current
        val buttonSize = entrySizeDp.dp
        val buttonSizePx = with(density) { buttonSize.toPx() }
        val marginPx = with(density) { entryMarginDp.dp.toPx() }
        // 可拖动范围：四周都保留贴边距离，保证按钮不会被贴到屏幕边缘
        val maxOffsetX = (with(density) { maxWidth.toPx() } - buttonSizePx - marginPx)
            .coerceAtLeast(marginPx)
        val maxOffsetY = (with(density) { maxHeight.toPx() } - buttonSizePx - marginPx)
            .coerceAtLeast(marginPx)
        // 圆形入口位置：默认左上角留贴边距离，拖动后记住 px 坐标
        var entryOffset by remember(maxOffsetX, maxOffsetY, marginPx, entryLayoutVersion) {
            val savedX = MoodEntryStore.getOffsetX(entryContext)
            val savedY = MoodEntryStore.getOffsetY(entryContext)
            mutableStateOf(
                if (savedX >= 0f && savedY >= 0f) {
                    Offset(
                        savedX.coerceIn(marginPx, maxOffsetX),
                        savedY.coerceIn(marginPx, maxOffsetY)
                    )
                } else {
                    Offset(marginPx, marginPx)
                }
            )
        }

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
                                            hasDiary = viewModel.hasDiaryOn(date),
                                            latestMood = viewModel.latestMoodForDate(date),
                                            moodCount = viewModel.moodCountForDate(date),
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_mood_happy),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Text(" 心情", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(5.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                Text(" 日记", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }

        // 旧版横条「心情一览」入口（设置中可开关，可与圆形按钮同时使用）
        if (showLegacyEntry) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    .clickable { onOpenMoods() }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "心情一览 · 共 ${viewModel.totalMoodCount()} 条记录",
                    modifier = Modifier.weight(1f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "›",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // 选中日期详情
        selectedDate?.let { date ->
            SelectedDayCard(
                date = date,
                moodTypes = viewModel.moodTypes,
                moodLabelOf = { viewModel.moodLabelOf(it) },
                moods = viewModel.moodsForDate(date),
                diaries = viewModel.diariesForDate(date),
                marks = viewModel.marksForDate(date).filter { it.type != CalendarMark.TYPE_MOOD },
                onAddMood = { time, mood, note -> viewModel.addMood(date, time, mood, note) },
                onAddCustomMood = { label, uri -> viewModel.addCustomMood(label, uri) },
                onUpdateMoodNote = { markId, note -> viewModel.updateMoodNote(markId, note) },
                onDeleteMood = { viewModel.deleteMark(it) },
                onOpenDiary = onOpenDiary,
                onWriteDiary = { onWriteDiary(date) },
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

        // 左上角圆形「心情一览」入口（可在设置中开启拖动）
        MoodEntryButton(
            modifier = Modifier
                .offset { IntOffset(entryOffset.x.roundToInt(), entryOffset.y.roundToInt()) }
                .pointerInput(entryDragEnabled, maxOffsetX, maxOffsetY, marginPx) {
                    if (entryDragEnabled) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                entryOffset = Offset(
                                    (entryOffset.x + dragAmount.x)
                                        .coerceIn(marginPx, maxOffsetX),
                                    (entryOffset.y + dragAmount.y)
                                        .coerceIn(marginPx, maxOffsetY)
                                )
                            },
                            onDragEnd = {
                                MoodEntryStore.saveOffset(entryContext, entryOffset.x, entryOffset.y)
                            },
                            onDragCancel = {
                                MoodEntryStore.saveOffset(entryContext, entryOffset.x, entryOffset.y)
                            }
                        )
                    }
                }
                .clickable { onOpenMoods() },
            iconVersion = entryIconVersion,
            draggable = entryDragEnabled,
            sizeDp = entrySizeDp
        )

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
        CalendarSettingsPanel(
            sensitivity = viewModel.swipeSensitivity,
            onSensitivityChange = { viewModel.updateSwipeSensitivity(it) },
            panelTransparency = settingsTransparency,
            onPanelTransparencyChange = { percent ->
                MoodEntryStore.setSettingsTransparency(entryContext, percent)
                settingsTransparency = MoodEntryStore.getSettingsTransparency(entryContext)
            },
            moodTypes = viewModel.moodTypes,
            onAddCustomMood = { label, uri -> viewModel.addCustomMood(label, uri) },
            onRenameMood = { moodId, label -> viewModel.renameMood(moodId, label) },
            onDeleteMoodType = { viewModel.deleteMoodType(it) },
            moodRecordCount = { viewModel.moodRecordCount(it) },
            entryDragEnabled = entryDragEnabled,
            onEntryDragEnabledChange = { enabled ->
                MoodEntryStore.setDragEnabled(entryContext, enabled)
                entryDragEnabled = enabled
            },
            entrySizeDp = entrySizeDp,
            onEntrySizeChange = { size ->
                MoodEntryStore.setButtonSizeDp(entryContext, size)
                entrySizeDp = MoodEntryStore.getButtonSizeDp(entryContext)
            },
            entryMarginDp = entryMarginDp,
            onEntryMarginChange = { margin ->
                MoodEntryStore.setEdgeMarginDp(entryContext, margin)
                entryMarginDp = MoodEntryStore.getEdgeMarginDp(entryContext)
            },
            legacyEntryVisible = showLegacyEntry,
            onLegacyEntryChange = { visible ->
                MoodEntryStore.setLegacyEntryVisible(entryContext, visible)
                showLegacyEntry = visible
            },
            onEntryIconChanged = { entryIconVersion++ },
            onRestoreLayout = { layout ->
                MoodEntryStore.applyLayout(entryContext, layout)
                entrySizeDp = MoodEntryStore.getButtonSizeDp(entryContext)
                entryMarginDp = MoodEntryStore.getEdgeMarginDp(entryContext)
                showLegacyEntry = MoodEntryStore.isLegacyEntryVisible(entryContext)
                entryLayoutVersion++
            },
            onResetLayout = {
                MoodEntryStore.resetLayout(entryContext)
                entrySizeDp = MoodEntryStore.getButtonSizeDp(entryContext)
                entryMarginDp = MoodEntryStore.getEdgeMarginDp(entryContext)
                showLegacyEntry = MoodEntryStore.isLegacyEntryVisible(entryContext)
                entryLayoutVersion++
            },
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
}

@Composable
private fun DayCell(
    date: LocalDate,
    isToday: Boolean,
    isSelected: Boolean,
    hasPeriod: Boolean,
    hasAnniversary: Boolean,
    hasDiary: Boolean,
    latestMood: String?,
    moodCount: Int,
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
        if (latestMood != null) {
            // 有心情记录时：显示当天最新心情表情，日期数字在下
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = moodIconPainter(latestMood),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = date.dayOfMonth.toString(),
                    fontSize = 11.sp,
                    lineHeight = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
            if (moodCount > 1) {
                // 多条心情时右上角显示数量角标
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 1.dp, top = 1.dp)
                        .size(12.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = moodCount.coerceAtMost(9).let { if (moodCount > 9) "9+" else it.toString() },
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        } else {
            Text(
                text = date.dayOfMonth.toString(),
                fontSize = 14.sp,
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        }
        if (hasDiary) {
            // 有日记的日期：左下角显示蓝色小圆点
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 4.dp, bottom = 3.dp)
                    .size(5.dp)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        }
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
    moodTypes: List<MoodType>,
    moodLabelOf: (String?) -> String?,
    moods: List<CalendarMark>,
    diaries: List<Diary>,
    marks: List<CalendarMark>,
    onAddMood: (time: String, mood: String, note: String?) -> Unit,
    onAddCustomMood: (label: String, iconUri: Uri?) -> Unit,
    onUpdateMoodNote: (markId: Long, note: String?) -> Unit,
    onDeleteMood: (Long) -> Unit,
    onOpenDiary: (Long) -> Unit,
    onWriteDiary: () -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val canRecordMood = !date.isAfter(LocalDate.now())
    var showAddMoodDialog by remember { mutableStateOf(false) }
    // 记录心情时使用的时刻，默认当前时间，可点击修改
    var selectedTime by remember(date) {
        mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
    }
    // 本次记录的心情描述（原因）
    var moodNote by remember(date) { mutableStateOf("") }
    // 待确认删除 / 待编辑描述的心情记录
    var deletingMood by remember { mutableStateOf<CalendarMark?>(null) }
    var editingMood by remember { mutableStateOf<CalendarMark?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("yyyy年M月d日")),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (moods.isNotEmpty()) {
                    Text(
                        text = "· 心情 ${moods.size} 条",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }

            // 心情记录时刻选择
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "记录时刻",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = selectedTime,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (canRecordMood) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier
                        .clickable(enabled = canRecordMood) {
                            val parts = selectedTime.split(":")
                            val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
                            val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
                            android.app.TimePickerDialog(
                                context,
                                { _, h, m ->
                                    selectedTime = String.format("%02d:%02d", h, m)
                                },
                                hour, minute, true
                            ).show()
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Text(
                text = "点击表情，在上述时刻记录一条心情",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )
            OutlinedTextField(
                value = moodNote,
                onValueChange = { if (it.length <= 60) moodNote = it },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                singleLine = true,
                enabled = canRecordMood,
                label = { Text("这一刻的想法（可选）", fontSize = 12.sp) },
                placeholder = { Text("记下心情的原因，方便以后回想", fontSize = 12.sp) }
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(moodTypes, key = { it.id }) { mood ->
                    Column(
                        modifier = Modifier
                            .width(72.dp)
                            .background(Color.Transparent, RoundedCornerShape(10.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = canRecordMood) {
                                onAddMood(selectedTime, mood.id, moodNote.trim().ifBlank { null })
                                moodNote = ""
                            }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = moodIconPainter(mood.id),
                            contentDescription = mood.label,
                            modifier = Modifier.size(30.dp)
                        )
                        Text(
                            text = mood.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .width(72.dp)
                            .background(Color.Transparent, RoundedCornerShape(10.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                RoundedCornerShape(10.dp)
                            )
                            .clickable(enabled = canRecordMood) { showAddMoodDialog = true }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "新增自定义心情",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "自定义",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            if (showAddMoodDialog) {
                AddMoodTypeDialog(
                    onDismiss = { showAddMoodDialog = false },
                    onConfirm = { label, uri ->
                        onAddCustomMood(label, uri)
                        showAddMoodDialog = false
                    }
                )
            }
            Text(
                text = if (canRecordMood) "一天可以在不同时刻记录多条心情，每条都能单独删除"
                else "未来日期不能记录心情",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(top = 6.dp)
            )

            // 已记录的心情列表
            if (moods.isNotEmpty()) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    moods.forEach { moodMark ->
                        val moodLabel = moodLabelOf(moodMark.name) ?: "已删除的心情"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { editingMood = moodMark }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = moodMark.time ?: "00:00",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(48.dp)
                            )
                            Image(
                                painter = moodIconPainter(moodMark.name),
                                contentDescription = moodLabel,
                                modifier = Modifier
                                    .size(22.dp)
                                    .padding(end = 6.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = moodLabel,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                                if (!moodMark.note.isNullOrBlank()) {
                                    Text(
                                        text = moodMark.note,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 1.dp)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { deletingMood = moodMark },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "删除这条心情",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 编辑心情描述
            editingMood?.let { mark ->
                var text by remember(mark.id) { mutableStateOf(mark.note ?: "") }
                AlertDialog(
                    onDismissRequest = { editingMood = null },
                    title = { Text("心情描述", fontWeight = FontWeight.Bold) },
                    text = {
                        Column {
                            Text(
                                text = "${mark.time ?: "00:00"} · ${moodLabelOf(mark.name) ?: "已删除的心情"}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            OutlinedTextField(
                                value = text,
                                onValueChange = { if (it.length <= 100) text = it },
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                minLines = 3,
                                label = { Text("记录原因（可选）", fontSize = 12.sp) },
                                placeholder = { Text("记下当时为什么是这个心情", fontSize = 12.sp) }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            onUpdateMoodNote(mark.id, text.trim().ifBlank { null })
                            editingMood = null
                        }) { Text("保存") }
                    },
                    dismissButton = {
                        TextButton(onClick = { editingMood = null }) { Text("取消") }
                    }
                )
            }

            // 删除前确认，防止误删
            deletingMood?.let { mark ->
                AlertDialog(
                    onDismissRequest = { deletingMood = null },
                    title = { Text("删除这条心情记录？", fontWeight = FontWeight.Bold) },
                    text = {
                        Text(
                            text = "${mark.time ?: "00:00"} · ${moodLabelOf(mark.name) ?: "已删除的心情"}" +
                                if (!mark.note.isNullOrBlank()) "\n${mark.note}" else ""
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            onDeleteMood(mark.id)
                            deletingMood = null
                        }) {
                            Text("删除", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { deletingMood = null }) { Text("取消") }
                    }
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "当天日记（${diaries.size}）",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = onWriteDiary) {
                    Text("写日记", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (diaries.isEmpty()) {
                Text(
                    text = "当天还没有日记，点「写日记」记下这一天吧",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            } else {
                diaries.forEach { diary ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDiary(diary.id) }
                            .padding(vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = diary.notebookName.ifBlank { "无标题日记" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = diary.date.takeLast(5),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        if (diary.content.isNotBlank()) {
                            Text(
                                text = diary.content.replace("\n", " "),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (marks.isEmpty()) {
                Text(
                    text = "当天没有其他记录，点右上角 + 添加",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
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
internal fun AddMoodTypeDialog(
    onDismiss: () -> Unit,
    onConfirm: (label: String, iconUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    var label by remember { mutableStateOf("") }
    var iconUri by remember { mutableStateOf<Uri?>(null) }
    val pickIcon = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) iconUri = uri }

    // 选好的图片预览
    val preview: Painter? = iconUri?.let { uri ->
        remember(uri) {
            runCatching {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)?.asImageBitmap()
                }
            }.getOrNull()?.let { BitmapPainter(it) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增自定义心情", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { if (it.length <= 6) label = it },
                    label = { Text("心情名称（最多 6 个字）") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (preview != null) {
                            Image(
                                painter = preview,
                                contentDescription = null,
                                modifier = Modifier.size(38.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_mood_custom),
                                contentDescription = null,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    TextButton(onClick = { pickIcon.launch("image/*") }) {
                        Text(if (iconUri == null) "选择表情图片" else "重新选择")
                    }
                }
                Text(
                    text = "不选图片时使用默认表情，之后也可在「日历设置」中更换",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = label.isNotBlank(),
                onClick = { onConfirm(label.trim(), iconUri) }
            ) { Text("添加") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
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
