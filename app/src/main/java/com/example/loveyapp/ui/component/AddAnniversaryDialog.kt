package com.example.loveyapp.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.loveyapp.util.CalendarType
import com.example.loveyapp.util.LunarCalendarUtil
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAnniversaryDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, date: String, calendarType: String) -> Unit,
    initialName: String = "",
    initialDate: String = "",
    initialCalendarType: String = "SOLAR"
) {
    val isEdit = initialName.isNotBlank() || initialDate.isNotBlank()
    var name by remember { mutableStateOf(initialName) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var calendarType by remember { mutableStateOf(initialCalendarType) }
    var displayDate by remember { mutableStateOf("") }
    var alternateDate by remember { mutableStateOf("") }
    LaunchedEffect(initialDate, initialCalendarType) {
        if (initialDate.isNotBlank()) {
            try {
                val localDate = LocalDate.parse(initialDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                if (initialCalendarType == "SOLAR") {
                    displayDate = initialDate
                    alternateDate = LunarCalendarUtil.solarToLunar(localDate)
                } else {
                    displayDate = LunarCalendarUtil.solarToLunar(localDate)
                    alternateDate = initialDate
                }
            } catch (e: Exception) {
                displayDate = initialDate
                alternateDate = ""
            }
        }
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (initialDate.isNotBlank()) {
            try {
                val localDate = LocalDate.parse(initialDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                if (initialCalendarType == "LUNAR") {
                    val lunar = LunarCalendarUtil.solarToLunar(localDate.year, localDate.monthValue, localDate.dayOfMonth)
                    LocalDate.of(lunar[0], lunar[1], lunar[2])
                } else {
                    localDate
                }.atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        AnimatedVisibility(
            visible = true,
            enter = scaleIn(
                initialScale = 0.9f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ),
            exit = scaleOut(
                targetScale = 0.9f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) + fadeOut(
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(24.dp)
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    .semantics {
                        contentDescription = if (isEdit) "编辑纪念日对话框" else "添加纪念日对话框"
                    }
            ) {
                Text(
                    text = if (isEdit) "编辑纪念日" else "添加纪念日",
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("纪念日名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = "日历类型",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val types = CalendarType.values()
                        for (index in types.indices) {
                            val type = types[index]
                            Button(
                                onClick = {
                                    calendarType = type.name
                                    if (selectedDate.isNotBlank()) {
                                        try {
                                            val localDate = LocalDate.parse(selectedDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                            if (type.name == "SOLAR") {
                                                displayDate = selectedDate
                                                alternateDate = LunarCalendarUtil.solarToLunar(localDate)
                                            } else {
                                                displayDate = LunarCalendarUtil.solarToLunar(localDate)
                                                alternateDate = selectedDate
                                            }
                                        } catch (e: Exception) {
                                            displayDate = selectedDate
                                            alternateDate = ""
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = if (index == types.size - 1) 0.dp else 8.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = if (calendarType == type.name) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    contentColor = if (calendarType == type.name) {
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
                }

                OutlinedTextField(
                    value = displayDate,
                    onValueChange = {},
                    label = { Text("选择日期") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    readOnly = true,
                    trailingIcon = {
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("选择")
                        }
                    },
                    singleLine = true
                )

                if (alternateDate.isNotBlank()) {
                    Text(
                        text = if (calendarType == "SOLAR") "农历: $alternateDate" else "公历: $alternateDate",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank() && selectedDate.isNotBlank()) {
                                onSave(name, selectedDate, calendarType)
                            }
                        },
                        enabled = name.isNotBlank() && selectedDate.isNotBlank(),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                        if (calendarType == "SOLAR") {
                            selectedDate = formattedDate
                            displayDate = formattedDate
                            alternateDate = LunarCalendarUtil.solarToLunar(date)
                        } else {
                            val lunarYear = date.year
                            val lunarMonth = date.monthValue
                            val lunarDay = date.dayOfMonth
                            val solarDate = LunarCalendarUtil.lunarToSolar(lunarYear, lunarMonth, lunarDay)
                            val solarFormatted = solarDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            selectedDate = solarFormatted
                            displayDate = LunarCalendarUtil.solarToLunar(solarDate)
                            alternateDate = solarFormatted
                        }
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
            DatePicker(state = datePickerState)
        }
    }
}
