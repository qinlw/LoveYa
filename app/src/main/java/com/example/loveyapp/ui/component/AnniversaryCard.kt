package com.example.loveyapp.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loveyapp.ui.viewmodel.AnniversaryWithDays
import com.example.loveyapp.util.CalendarType
import com.example.loveyapp.util.DisplayCalendarMode
import com.example.loveyapp.util.LunarCalendarUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnniversaryCard(
    item: AnniversaryWithDays,
    onClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    displayCalendarMode: DisplayCalendarMode = DisplayCalendarMode.BOTH,
    index: Int = 0
) {
    val daysColor = when {
        item.daysRemaining <= 7 -> MaterialTheme.colorScheme.error
        item.daysRemaining <= 30 -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.primary
    }

    var isPressed by remember { mutableStateOf(false) }
    // 长按弹出的选项菜单是否展示
    var showMenu by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) MaterialTheme.colorScheme.surfaceVariant
                      else MaterialTheme.colorScheme.surface,
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
    )

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val showYear = item.config.showYear
    val (solarText, lunarText) = try {
        val ld = LocalDate.parse(item.config.targetDate, dateFormatter)
        // 根据 showYear 决定公历是否带年份
        val solar = if (showYear) {
            "公历 $ld"
        } else {
            "公历 ${ld.format(DateTimeFormatter.ofPattern("MM月dd日"))}"
        }
        // 农历字符串形如 "癸未羊年九月廿五"，showYear=false 时去掉年份前缀
        val lunarFull = "农历 ${LunarCalendarUtil.solarToLunar(ld)}"
        val lunar = if (showYear) {
            lunarFull
        } else {
            val lunarPart = LunarCalendarUtil.solarToLunar(ld)
            // 去掉首个"年"及其之前内容，仅保留月日部分
            val mdPart = lunarPart.substringAfter("年")
            "农历 $mdPart"
        }
        solar to lunar
    } catch (_: Exception) {
        item.config.targetDate to ""
    }

    val displayText = when (displayCalendarMode) {
        DisplayCalendarMode.SOLAR_ONLY -> solarText
        DisplayCalendarMode.LUNAR_ONLY -> lunarText.ifBlank { solarText }
        DisplayCalendarMode.BOTH -> {
            val separator = if (item.config.calendarType == CalendarType.LUNAR.name) {
                if (lunarText.isNotBlank()) "$lunarText  |  $solarText" else solarText
            } else {
                if (lunarText.isNotBlank()) "$solarText  |  $lunarText" else solarText
            }
            separator
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    bounded = true,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                ),
                onClick = onClick,
                onLongClick = {
                    // 长按弹出编辑/删除菜单
                    showMenu = true
                }
            )
            .padding(16.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.config.name,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // 长按触发的下拉菜单：编辑 / 删除
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("编辑") },
                    onClick = {
                        showMenu = false
                        onEdit()
                    }
                )
                DropdownMenuItem(
                    text = { Text("删除") },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayText,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Text(
                text = if (item.isExpired) "已过${-item.daysRemaining}天" else "还有${item.daysRemaining}天",
                fontSize = 18.sp,
                color = daysColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
    }
}
