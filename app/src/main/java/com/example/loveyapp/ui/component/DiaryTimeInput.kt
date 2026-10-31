package com.example.loveyapp.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 当前的日记时间，格式：yyyy-MM-dd HH:mm */
fun currentDiaryDateTime(): String =
    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

/** 把存储值拆成（日期 yyyy-MM-dd，时间 HH:mm），兼容旧版本只有日期的数据 */
fun splitDiaryDateTime(raw: String): Pair<String, String> {
    val date = raw.trim().take(10).takeIf { it.isNotBlank() }
        ?: LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val time = raw.trim().drop(11).take(5)
        .takeIf { it.length == 5 && it != "" }
        ?: "00:00"
    return date to time
}

/** 统一成 yyyy-MM-dd HH:mm，便于比较是否有修改（旧数据补 00:00） */
fun normalizeDiaryDateTime(raw: String): String {
    val (date, time) = splitDiaryDateTime(raw)
    return "$date $time"
}

/**
 * 日记时间（必填项）：日期使用项目已有的公历/农历选择器，时刻单独选择。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryTimeInput(
    date: String,
    time: String,
    onDateSelected: (String) -> Unit,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "日记时间（必填）",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        DatePicker(
            label = "日记日期",
            selectedDate = date,
            calendarType = "SOLAR",
            onDateSelected = { solarDate, _ -> onDateSelected(solarDate) }
        )

        TimeField(
            time = time,
            onTimeSelected = onTimeSelected
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeField(
    time: String,
    onTimeSelected: (String) -> Unit
) {
    var showTimePicker by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = time,
        onValueChange = {},
        label = { Text("日记时刻") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        readOnly = true,
        trailingIcon = {
            TextButton(onClick = { showTimePicker = true }) {
                Text("选择")
            }
        }
    )

    if (showTimePicker) {
        val parsed = runCatching { LocalTime.parse(time) }.getOrNull() ?: LocalTime.now()
        val timeState = rememberTimePickerState(
            initialHour = parsed.hour,
            initialMinute = parsed.minute,
            is24Hour = true
        )
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "选择时刻",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TimePicker(state = timeState)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("取消")
                        }
                        TextButton(
                            onClick = {
                                onTimeSelected(
                                    String.format(
                                        Locale.US,
                                        "%02d:%02d",
                                        timeState.hour,
                                        timeState.minute
                                    )
                                )
                                showTimePicker = false
                            }
                        ) {
                            Text("确认")
                        }
                    }
                }
            }
        }
    }
}
