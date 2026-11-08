package com.example.loveyapp.ui.screen

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loveyapp.R
import com.example.loveyapp.data.local.entity.CalendarMark
import com.example.loveyapp.data.prefs.MoodIconStorage
import com.example.loveyapp.data.prefs.MoodType
import com.example.loveyapp.ui.viewmodel.MoodViewModel
import java.time.LocalDate

/** 内置心情对应的默认表情；自定义心情使用通用默认表情 */
private fun moodDefaultRes(moodId: String): Int = when (moodId) {
    CalendarMark.MOOD_HAPPY -> R.drawable.ic_mood_happy
    CalendarMark.MOOD_NORMAL -> R.drawable.ic_mood_normal
    CalendarMark.MOOD_SAD -> R.drawable.ic_mood_sad
    else -> R.drawable.ic_mood_custom
}

@Composable
private fun moodIconPainter(moodId: String): Painter {
    val context = LocalContext.current
    val customFile = MoodIconStorage.getCustomIconFile(context, moodId)
    return if (customFile.exists()) {
        val bitmap = remember(customFile.absolutePath, customFile.lastModified()) {
            runCatching {
                BitmapFactory.decodeFile(customFile.absolutePath)?.asImageBitmap()
            }.getOrNull()
        }
        if (bitmap != null) remember(bitmap) { BitmapPainter(bitmap) }
        else painterResource(id = moodDefaultRes(moodId))
    } else {
        painterResource(id = moodDefaultRes(moodId))
    }
}

/**
 * 心情一览：独立的心情总管理处。
 * 汇总所有日期的心情记录，支持按心情分类筛选，
 * 并可查找、恢复已删除的历史心情。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
    onNavigateBack: () -> Unit,
    viewModel: MoodViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) { viewModel.load() }

    var selectedMoodId by remember { mutableStateOf<String?>(null) }

    val filterMoods: List<MoodType> = viewModel.moodTypes + viewModel.hiddenMoods
    val records = remember(viewModel.moodMarks, selectedMoodId) {
        viewModel.moodMarks
            .filter { selectedMoodId == null || it.name == selectedMoodId }
            .sortedWith(
                compareByDescending<CalendarMark> { it.date }
                    .thenByDescending { it.time ?: "" }
            )
    }
    val grouped = records.groupBy { it.date }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("心情一览", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 已删除的心情：可查找、可恢复
            if (viewModel.hiddenMoods.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "已删除的心情",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "历史记录仍保留；恢复后会重新出现在心情选择列表",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                            modifier = Modifier.padding(top = 2.dp, bottom = 6.dp)
                        )
                        viewModel.hiddenMoods.forEach { mood ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = moodIconPainter(mood.id),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .padding(end = 6.dp)
                                )
                                Text(
                                    text = mood.label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = " · ${viewModel.countOf(mood.id)} 条记录",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "恢复",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable { viewModel.restoreMood(mood.id) }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 心情分类筛选
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    MoodFilterChip(
                        text = "全部",
                        selected = selectedMoodId == null,
                        onClick = { selectedMoodId = null }
                    )
                }
                items(filterMoods, key = { it.id }) { mood ->
                    MoodFilterChip(
                        text = mood.label + if (viewModel.isDeleted(mood.id)) "（已删除）" else "",
                        selected = selectedMoodId == mood.id,
                        onClick = { selectedMoodId = mood.id }
                    )
                }
            }

            Text(
                text = "共 ${records.size} 条心情记录",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            if (records.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "还没有心情记录\n去日历选中某一天记录心情吧",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    grouped.forEach { (dateText, dayRecords) ->
                        item(key = "date-$dateText") {
                            Text(
                                text = formatDate(dateText),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(dayRecords, key = { it.id }) { mark ->
                            val label = viewModel.labelOf(mark.name) ?: "已删除的心情"
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = mark.time ?: "00:00",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.width(48.dp)
                                )
                                Image(
                                    painter = moodIconPainter(mark.name),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(26.dp)
                                        .padding(end = 8.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = label + if (viewModel.isDeleted(mark.name)) "（已删除）" else "",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (!mark.note.isNullOrBlank()) {
                                        Text(
                                            text = mark.note,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                        item(key = "divider-$dateText") {
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MoodFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surface,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

/** 日期显示：2026-11-08 → 2026年11月8日 周日 */
private fun formatDate(dateText: String): String {
    return runCatching {
        val date = LocalDate.parse(dateText)
        val weekdays = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        "${date.year}年${date.monthValue}月${date.dayOfMonth}日 ${weekdays[date.dayOfWeek.value - 1]}"
    }.getOrDefault(dateText)
}
