package com.example.loveyapp.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.example.loveyapp.util.CalendarType
import com.example.loveyapp.util.LunarCalendarUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePicker(
    label: String,
    selectedDate: String,
    calendarType: String,
    onDateSelected: (String, String) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var currentCalendarType by remember { mutableStateOf(calendarType) }
    var selectedDateValue by remember { mutableStateOf(selectedDate) }
    var displayDateValue by remember { mutableStateOf("") }
    var alternateDateValue by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(selectedDate, currentCalendarType) {
        if (selectedDate.isNotBlank()) {
            try {
                val localDate = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                if (currentCalendarType == "SOLAR") {
                    displayDateValue = selectedDate
                    alternateDateValue = LunarCalendarUtil.solarToLunar(localDate)
                } else {
                    displayDateValue = LunarCalendarUtil.solarToLunar(localDate)
                    alternateDateValue = selectedDate
                }
            } catch (e: Exception) {
                displayDateValue = selectedDate
                alternateDateValue = ""
            }
        } else {
            displayDateValue = ""
            alternateDateValue = ""
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val types = CalendarType.values()
            for (index in types.indices) {
                val type = types[index]
                Button(
                    onClick = {
                        if (selectedDate.isNotBlank()) {
                            try {
                                val localDate = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                if (type.name == "SOLAR") {
                                    displayDateValue = selectedDate
                                    alternateDateValue = LunarCalendarUtil.solarToLunar(localDate)
                                } else {
                                    displayDateValue = LunarCalendarUtil.solarToLunar(localDate)
                                    alternateDateValue = selectedDate
                                }
                            } catch (e: Exception) {
                                displayDateValue = selectedDate
                                alternateDateValue = ""
                            }
                        }
                        currentCalendarType = type.name
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentCalendarType == type.name) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        contentColor = if (currentCalendarType == type.name) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                ) {
                    Text(if (type == CalendarType.SOLAR) "公历" else "农历")
                }
            }
        }

        OutlinedTextField(
            value = displayDateValue,
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            readOnly = true,
            trailingIcon = {
                TextButton(onClick = { showDatePicker = true }) {
                    Text("选择")
                }
            }
        )

        if (alternateDateValue.isNotBlank()) {
            Text(
                text = if (currentCalendarType == "SOLAR") "农历: $alternateDateValue" else "公历: $alternateDateValue",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }

    if (showDatePicker) {
        if (currentCalendarType == "LUNAR") {
            // 农历模式：使用真正的农历日期选择器（年/月/日 三列）
            LunarDatePickerDialog(
                initialSolarDate = selectedDateValue.ifBlank {
                    java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                },
                onConfirm = { lunarDisplay, solarFormatted ->
                    selectedDateValue = solarFormatted
                    displayDateValue = lunarDisplay
                    alternateDateValue = solarFormatted
                    onDateSelected(solarFormatted, currentCalendarType)
                    showDatePicker = false
                },
                onDismiss = { showDatePicker = false }
            )
        } else {
            // 公历模式：使用 Material3 公历选择器
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            selectedDateValue = formattedDate
                            displayDateValue = formattedDate
                            alternateDateValue = LunarCalendarUtil.solarToLunar(date)
                            onDateSelected(formattedDate, currentCalendarType)
                        }
                        showDatePicker = false
                    }) {
                        Text("确认")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("取消")
                    }
                }
            ) {
                androidx.compose.material3.DatePicker(state = datePickerState)
            }
        }
    }
}