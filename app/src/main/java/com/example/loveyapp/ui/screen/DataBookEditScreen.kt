package com.example.loveyapp.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

    val dataBook = viewModel.currentDataBook
    if (dataBook != null && name.isEmpty()) {
        name = dataBook.name
        content = dataBook.content
        tags = dataBook.tags
    }

    var saveMessage by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

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
                onClick = {
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
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                enabled = !viewModel.isLoading && name.isNotBlank() && content.isNotBlank()
            ) {
                Text(if (dataBookId != null) "保存修改" else "保存")
            }

            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("取消")
            }
        }
    }

    // 屏幕正中悬浮提示
    com.example.loveyapp.ui.component.ToastPopup(
        message = saveMessage,
        isSuccess = true,
        onDismiss = { saveMessage = null }
    )
}
