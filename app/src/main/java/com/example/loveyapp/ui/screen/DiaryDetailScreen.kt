package com.example.loveyapp.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loveyapp.ui.component.ToastPopup
import com.example.loveyapp.ui.viewmodel.DiaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryDetailScreen(
    diaryId: Long?,
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit,
    onDeleteSuccess: () -> Unit
) {
    val viewModel: DiaryViewModel = hiltViewModel()
    var isContentVisible by remember { mutableStateOf(false) }
    // 可编辑的日记标题
    var editableTitle by remember { mutableStateOf("") }
    // 可编辑的内容文本
    var editableContent by remember { mutableStateOf("") }
    // 保存反馈提示
    var saveMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(diaryId) {
        if (diaryId != null && viewModel.currentDiary == null) {
            viewModel.getDiaryById(diaryId)
        }
        isContentVisible = true
    }

    val diary = viewModel.currentDiary
    // 日记加载后初始化可编辑字段
    LaunchedEffect(diary?.id) {
        if (diary != null) {
            if (editableTitle.isEmpty()) editableTitle = diary.notebookName
            if (editableContent.isEmpty()) editableContent = diary.content
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("详情") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    // 右上角"保存"按钮：同时保存日记标题和内容
                    TextButton(
                        onClick = {
                            diary?.let { d ->
                                val titleToSave = editableTitle.trim().ifEmpty { d.notebookName }
                                viewModel.updateDiaryContentAndTitle(
                                    d.id,
                                    titleToSave,
                                    editableContent
                                ) {
                                    saveMessage = "保存成功"
                                }
                            }
                        },
                        enabled = diary != null && !viewModel.isLoading
                    ) {
                        Text("保存", color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                if (viewModel.isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (diary != null) {
                    AnimatedVisibility(
                        visible = isContentVisible,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { 30 })
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(24.dp)),
                            elevation = CardDefaults.cardElevation(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // 日记标题区域：浅蓝色渐变背景填充整个区域（与数据手册一致）
                                // 高度约 120dp
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.secondary
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    OutlinedTextField(
                                        value = editableTitle,
                                        onValueChange = { editableTitle = it },
                                        modifier = Modifier.fillMaxWidth(),
                                        textStyle = TextStyle(
                                            fontSize = 24.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        ),
                                        placeholder = {
                                            Text(
                                                text = "日记标题",
                                                color = Color.White.copy(alpha = 0.7f),
                                                fontSize = 24.sp,
                                                fontWeight = FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                        },
                                        singleLine = true,
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color.Transparent,
                                            unfocusedContainerColor = Color.Transparent,
                                            disabledContainerColor = Color.Transparent,
                                            focusedIndicatorColor = Color.Transparent,
                                            unfocusedIndicatorColor = Color.Transparent,
                                            cursorColor = Color.White
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // 日记日期居中展示
                                Text(
                                    text = diary.date,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Divider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    thickness = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Text(
                                        text = "日记内容",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                        fontWeight = FontWeight.SemiBold
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // 内容可编辑：左对齐、首行缩进2个字符
                                    // 增加默认最小高度，让内容区域覆盖到屏幕底部约 2cm
                                    OutlinedTextField(
                                        value = editableContent,
                                        onValueChange = { editableContent = it },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .defaultMinSize(minHeight = 380.dp),
                                        textStyle = TextStyle(
                                            fontSize = 18.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 28.sp,
                                            textAlign = TextAlign.Start,
                                            textIndent = TextIndent(firstLine = 24.sp)
                                        ),
                                        minLines = 3
                                    )
                                }

                                if (diary.tags.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "标签：",
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = diary.tags,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = "日记不存在")
                    }
                }

                // 错误提示
                viewModel.error?.let { errMsg ->
                    Text(
                        text = errMsg,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            // 屏幕正中悬浮提示
            ToastPopup(
                message = saveMessage,
                isSuccess = true,
                onDismiss = { saveMessage = null }
            )
        }
    }
}
