package com.example.loveyapp.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loveyapp.ui.component.DiaryTimeInput
import com.example.loveyapp.ui.component.currentDiaryDateTime
import com.example.loveyapp.ui.component.normalizeDiaryDateTime
import com.example.loveyapp.ui.component.splitDiaryDateTime
import com.example.loveyapp.ui.viewmodel.DiaryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DiaryEditScreen(
    diaryId: Long?,
    onSaveSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: DiaryViewModel = hiltViewModel()

    var notebookName by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    // 日记时间（必填）：默认取当前时间，保存后仍可修改
    val initialDateTime = remember { currentDiaryDateTime() }
    val initialSplit = remember(initialDateTime) { splitDiaryDateTime(initialDateTime) }
    var diaryDate by remember { mutableStateOf(initialSplit.first) }
    var diaryTime by remember { mutableStateOf(initialSplit.second) }

    androidx.compose.runtime.LaunchedEffect(diaryId) {
        if (diaryId != null && viewModel.currentDiary == null) {
            viewModel.getDiaryById(diaryId)
        }
    }

    // 加载完成后的原始内容，用于判断是否有未保存的修改
    var originalTitle by remember { mutableStateOf("") }
    var originalContent by remember { mutableStateOf("") }
    var originalTags by remember { mutableStateOf("") }
    var originalDateTime by remember { mutableStateOf(initialDateTime) }

    val diary = viewModel.currentDiary
    var fieldsInitialized by remember { mutableStateOf(false) }
    if (diary != null && !fieldsInitialized) {
        notebookName = diary.notebookName
        content = diary.content
        tags = diary.tags
        originalTitle = diary.notebookName
        originalContent = diary.content
        originalTags = diary.tags
        val (loadedDate, loadedTime) = splitDiaryDateTime(diary.date)
        diaryDate = loadedDate
        diaryTime = loadedTime
        originalDateTime = normalizeDiaryDateTime(diary.date)
        fieldsInitialized = true
    }

    // 新建日记时作为 placeholder 显示的默认标题，不占用真正的标题框
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val defaultTitle = "我的日记${today}"

    var saveMessage by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

    val hasUnsavedChanges = notebookName != originalTitle ||
        content != originalContent ||
        tags != originalTags ||
        "$diaryDate $diaryTime" != originalDateTime
    val canSave = content.isNotBlank() && diaryDate.isNotBlank() && !viewModel.isLoading
    var showExitDialog by remember { mutableStateOf(false) }

    val saveDiary: () -> Unit = {
        // 标题为空时使用默认标题
        val titleToSave = notebookName.ifBlank { defaultTitle }
        val dateTimeToSave = "$diaryDate $diaryTime"
        if (diaryId != null) {
            viewModel.updateDiary(diaryId, titleToSave, content, tags, dateTimeToSave) {
                saveMessage = "保存成功"
                onSaveSuccess()
            }
        } else {
            viewModel.addDiary(titleToSave, content, tags, dateTimeToSave) {
                saveMessage = "保存成功"
                onSaveSuccess()
            }
        }
    }

    // 拦截系统返回：有未保存内容时先询问，避免直接退出丢失编辑
    BackHandler(enabled = hasUnsavedChanges) { showExitDialog = true }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = if (diaryId != null) "编辑日记" else "新建日记",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
        ) {
            OutlinedTextField(
                value = notebookName,
                onValueChange = { notebookName = it },
                label = { Text("日记标题") },
                placeholder = {
                    // 用户未输入时灰色显示默认标题，输入后自动消失，标题为空时重新显示
                    Text(
                        defaultTitle,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )

            DiaryTimeInput(
                date = diaryDate,
                time = diaryTime,
                onDateSelected = { diaryDate = it },
                onTimeSelected = { diaryTime = it },
                modifier = Modifier.padding(top = 16.dp)
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("日记内容") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                minLines = 10,
                maxLines = 50
            )

            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("标签") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            viewModel.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Button(
                onClick = saveDiary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                // 标题允许为空（用默认标题），仅内容必填
                enabled = canSave
            ) {
                Text(if (diaryId != null) "保存修改" else "保存")
            }

            Button(
                onClick = {
                    if (hasUnsavedChanges) {
                        showExitDialog = true
                    } else {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("取消")
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("尚未保存") },
            text = { Text("当前编辑的内容还没有保存，是否保存后退出？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        saveDiary()
                    },
                    enabled = canSave
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            showExitDialog = false
                            onNavigateBack()
                        }
                    ) {
                        Text("不保存")
                    }
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("继续编辑")
                    }
                }
            }
        )
    }

    // 屏幕正中悬浮提示
    com.example.loveyapp.ui.component.ToastPopup(
        message = saveMessage,
        isSuccess = true,
        onDismiss = { saveMessage = null }
    )
}
