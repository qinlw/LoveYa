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
import com.example.loveyapp.ui.viewmodel.DataBookViewModel

@Composable
fun DataBookEditScreen(
    dataBookId: Long?,
    onSaveSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: DataBookViewModel = hiltViewModel()

    var name by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(dataBookId) {
        if (dataBookId != null && viewModel.currentDataBook == null) {
            viewModel.getDataBookById(dataBookId)
        }
    }

    // 加载完成后的原始内容，用于判断是否有未保存的修改
    var originalName by remember { mutableStateOf("") }
    var originalContent by remember { mutableStateOf("") }
    var originalTags by remember { mutableStateOf("") }

    val dataBook = viewModel.currentDataBook
    var fieldsInitialized by remember { mutableStateOf(false) }
    if (dataBook != null && !fieldsInitialized) {
        name = dataBook.name
        content = dataBook.content
        tags = dataBook.tags
        originalName = dataBook.name
        originalContent = dataBook.content
        originalTags = dataBook.tags
        fieldsInitialized = true
    }

    var saveMessage by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

    val hasUnsavedChanges = name != originalName ||
        content != originalContent ||
        tags != originalTags
    val canSave = name.isNotBlank() && content.isNotBlank() && !viewModel.isLoading
    var showExitDialog by remember { mutableStateOf(false) }

    val saveDataBook: () -> Unit = {
        if (dataBookId != null) {
            viewModel.updateDataBook(dataBookId, name, content, tags) {
                saveMessage = "保存成功"
                onSaveSuccess()
            }
        } else {
            viewModel.addDataBook(name, content, tags) {
                saveMessage = "保存成功"
                onSaveSuccess()
            }
        }
    }

    // 拦截系统返回：有未保存内容时先询问，避免直接退出丢失编辑
    BackHandler(enabled = hasUnsavedChanges) { showExitDialog = true }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = if (dataBookId != null) "编辑手册" else "新手册",
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
                value = name,
                onValueChange = { name = it },
                label = { Text("手册名称") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("手册内容") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                minLines = 5,
                maxLines = 20
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
                onClick = saveDataBook,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                enabled = canSave
            ) {
                Text(if (dataBookId != null) "保存修改" else "保存")
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
                        saveDataBook()
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
