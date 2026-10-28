package com.example.loveyapp.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.loveyapp.ui.component.AddAnniversaryDialog
import com.example.loveyapp.ui.component.AnniversaryCard
import com.example.loveyapp.ui.component.SwipeRevealItem
import com.example.loveyapp.ui.viewmodel.AnniversaryViewModel
import com.example.loveyapp.ui.viewmodel.SettingsViewModel
import com.example.loveyapp.util.DisplayCalendarMode
import com.example.loveyapp.util.LunarCalendarUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    onNavigateToAddAnniversary: () -> Unit,
    navController: NavController,
    padding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(),
    onFabClick: () -> Unit = {}
) {
    val anniversaryViewModel: AnniversaryViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingAnniversary by remember { mutableStateOf<com.example.loveyapp.ui.viewmodel.AnniversaryWithDays?>(null) }
    var loveDays by remember { mutableStateOf(0) }
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE")

    LaunchedEffect(Unit) {
        settingsViewModel.loadUserInfo()
        anniversaryViewModel.loadAnniversaries()
        // 每次进入首页时从持久化存储重新加载日历设置，
        // 避免在其他页面（如设置页）修改后返回时仍显示旧值
        settingsViewModel.reloadCalendarSettings()
    }

    LaunchedEffect(settingsViewModel.userInfo) {
        settingsViewModel.userInfo?.anniversaryDate?.let { anniversaryDate ->
            if (anniversaryDate.isNotBlank()) {
                try {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val date = LocalDate.parse(anniversaryDate, formatter)
                    loveDays = ChronoUnit.DAYS.between(date, today).toInt().coerceAtLeast(0)
                } catch (e: Exception) {
                    loveDays = 0
                }
            } else {
                loveDays = 0
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val mode = settingsViewModel.displayCalendarMode
            val solarToday = today.format(dateFormatter)
            val lunarToday = try { LunarCalendarUtil.solarToLunar(today) } catch (_: Exception) { "" }
            when (mode) {
                DisplayCalendarMode.SOLAR_ONLY -> {
                    Text(
                        text = solarToday,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                DisplayCalendarMode.LUNAR_ONLY -> {
                    Text(
                        text = "农历 $lunarToday",
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                DisplayCalendarMode.BOTH -> {
                    // 公历与农历换行显示，避免过长时 UI 挤压变形
                    Text(
                        text = solarToday,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    if (lunarToday.isNotBlank()) {
                        Text(
                            text = "农历 $lunarToday",
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = today.format(dayOfWeekFormatter),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Text(
                text = "恋爱天数",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = loveDays.toString(),
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "天",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // 生日倒计时卡片：左“我的生日”，右“爱人生日”
        val userBirthday = settingsViewModel.userInfo?.userBirthday ?: ""
        val loverBirthday = settingsViewModel.userInfo?.loverBirthday ?: ""
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BirthdayCountdownCard(
                title = "我的生日",
                birthday = userBirthday,
                today = today,
                calendarType = settingsViewModel.myBirthdayCalendarType,
                onToggle = { settingsViewModel.toggleMyBirthdayCalendarType() },
                modifier = Modifier.weight(1f)
            )
            BirthdayCountdownCard(
                title = "爱人生日",
                birthday = loverBirthday,
                today = today,
                calendarType = settingsViewModel.loverBirthdayCalendarType,
                onToggle = { settingsViewModel.toggleLoverBirthdayCalendarType() },
                modifier = Modifier.weight(1f)
            )
        }

        Text(
            text = "即将到来的纪念日",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        if (anniversaryViewModel.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "加载中...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(anniversaryViewModel.anniversaries) { index, item ->
                    SwipeRevealItem(
                        actionText = if (item.config.isPinned) "取消置顶" else "置顶",
                        onAction = {
                            anniversaryViewModel.togglePin(item.config.id, !item.config.isPinned)
                        }
                    ) {
                        AnniversaryCard(
                            item = item,
                            index = index,
                            // 优先使用纪念日自己的 displayMode，不再使用全局 settingsViewModel.displayCalendarMode
                            displayCalendarMode = DisplayCalendarMode.fromValue(item.config.displayMode),
                            onEdit = {
                                editingAnniversary = item
                                showEditDialog = true
                            },
                            onDelete = {
                                anniversaryViewModel.deleteAnniversary(item.config.id) {}
                            }
                        )
                    }
                }

                if (anniversaryViewModel.anniversaries.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "暂无纪念日",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "点击下方按钮添加",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAnniversaryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, date, calendarType, displayMode, showYear ->
                anniversaryViewModel.addAnniversary(name, date, calendarType, displayMode, showYear) {
                    showAddDialog = false
                }
            }
        )
    }

    if (showEditDialog && editingAnniversary != null) {
        AddAnniversaryDialog(
            onDismiss = {
                showEditDialog = false
                editingAnniversary = null
            },
            onSave = { name, date, calendarType, displayMode, showYear ->
                editingAnniversary?.let { item ->
                    anniversaryViewModel.updateAnniversary(item.config.id, name, date, calendarType, displayMode, showYear) {
                        showEditDialog = false
                        editingAnniversary = null
                    }
                }
            },
            initialName = editingAnniversary!!.config.name,
            initialDate = editingAnniversary!!.config.targetDate,
            initialCalendarType = editingAnniversary!!.config.calendarType,
            initialDisplayMode = editingAnniversary!!.config.displayMode,
            initialShowYear = editingAnniversary!!.config.showYear
        )
    }
}

/**
 * 生日倒计时卡片。
 *
 * 点击标题可切换该生日的倒计时计算方式（公历 ↔ 农历）。
 * 公历模式：按公历月日计算下次生日
 * 农历模式：按农历月日计算下次生日（先转农历月日比较，再转回公历算天数）
 *
 * @param title 标题，如"我的生日"、"爱人生日"
 * @param birthday 生日字符串，格式 yyyy-MM-dd；为空时显示"未设置"
 * @param calendarType 当前使用的日历类型（公历/农历）
 * @param onToggle 点击标题时触发，用于切换日历类型
 */
@Composable
private fun BirthdayCountdownCard(
    title: String,
    birthday: String,
    today: LocalDate,
    calendarType: com.example.loveyapp.util.CalendarType,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val countdownText = if (birthday.isBlank()) {
        "未设置"
    } else {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val bd = LocalDate.parse(birthday, formatter)

            val days = if (calendarType == com.example.loveyapp.util.CalendarType.SOLAR) {
                // 公历模式：用公历月日计算
                val thisYearBirthday = LocalDate.of(today.year, bd.monthValue, bd.dayOfMonth)
                val diff = ChronoUnit.DAYS.between(today, thisYearBirthday).toInt()
                if (diff >= 0) diff else {
                    val nextBirthday = thisYearBirthday.plusYears(1)
                    ChronoUnit.DAYS.between(today, nextBirthday).toInt()
                }
            } else {
                // 农历模式：按农历月日计算
                // 把生日（公历）转成农历月日
                val bdLunar = com.example.loveyapp.util.LunarCalendarUtil.solarToLunar(
                    bd.year, bd.monthValue, bd.dayOfMonth
                )
                val bdLunarMonth = bdLunar[1]
                val bdLunarDay = bdLunar[2]

                // 把今天转成农历月日
                val todayLunar = com.example.loveyapp.util.LunarCalendarUtil.solarToLunar(
                    today.year, today.monthValue, today.dayOfMonth
                )
                val todayLunarYear = todayLunar[0]
                val todayLunarMonth = todayLunar[1]
                val todayLunarDay = todayLunar[2]

                // 比较农历月日
                val todayMD = todayLunarMonth * 100 + todayLunarDay
                val bdMD = bdLunarMonth * 100 + bdLunarDay

                // 确定下次农历生日的年份
                val nextLunarYear = if (todayMD >= bdMD) todayLunarYear + 1 else todayLunarYear

                // 转回公历算天数差
                val nextSolar = com.example.loveyapp.util.LunarCalendarUtil.lunarToSolar(
                    nextLunarYear, bdLunarMonth, bdLunarDay
                )
                ChronoUnit.DAYS.between(today, nextSolar).toInt()
            }

            when {
                days == 0 -> "就是今天"
                days > 0 -> "还有${days}天"
                else -> "还有${-days}天"
            }
        } catch (_: Exception) {
            "未设置"
        }
    }

    val typeLabel = if (calendarType == com.example.loveyapp.util.CalendarType.SOLAR) "公历" else "农历"

    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = androidx.compose.material.ripple.rememberRipple(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        ),
                        onClick = onToggle
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = typeLabel,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Text(
                text = countdownText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
