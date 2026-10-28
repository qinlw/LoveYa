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

    androidx.compose.runtime.LaunchedEffect(diaryId) {
        if (diaryId != null && viewModel.currentDiary == null) {
            viewModel.getDiaryById(diaryId)
        }
    }

    val diary = viewModel.currentDiary
    var fieldsInitialized by remember { mutableStateOf(false) }
    if (diary != null && !fieldsInitialized) {
        notebookName = diary.notebookName
        content = diary.content
        tags = diary.tags
        fieldsInitialized = true
    }

    // 新建日记时作为 placeholder 显示的默认标题，不占用真正的标题框
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val defaultTitle = "我的日记${today}"

    var saveMessage by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }

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
                onClick = {
                    // 标题为空时使用默认标题
                    val titleToSave = notebookName.ifBlank { defaultTitle }
                    if (diaryId != null) {
                        viewModel.updateDiary(diaryId, titleToSave, content, tags) {
                            saveMessage = "保存成功"
                            onSaveSuccess()
                        }
                    } else {
                        viewModel.addDiary(titleToSave, content, tags) {
                            saveMessage = "保存成功"
                            onSaveSuccess()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                // 标题允许为空（用默认标题），仅内容必填
                enabled = !viewModel.isLoading && content.isNotBlank()
            ) {
                Text(if (diaryId != null) "保存修改" else "保存")
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
