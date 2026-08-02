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
import androidx.navigation.NavController
import com.example.loveyapp.ui.component.ToastPopup
import com.example.loveyapp.ui.viewmodel.DataBookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataBookDetailScreen(
    dataBookId: Long,
    onNavigateToEdit: () -> Unit,
    onNavigateBack: () -> Unit,
    navController: NavController
) {
    val viewModel: DataBookViewModel = hiltViewModel()
    var isContentVisible by remember { mutableStateOf(false) }
    // 可编辑的手册名称
    var editableName by remember { mutableStateOf("") }
    // 可编辑的内容文本
    var editableContent by remember { mutableStateOf("") }
    // 保存反馈提示
    var saveMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(dataBookId) {
        viewModel.getDataBookById(dataBookId)
        isContentVisible = true
    }

    val dataBook = viewModel.currentDataBook
    // 数据手册加载后初始化可编辑字段
    LaunchedEffect(dataBook?.id) {
        if (dataBook != null) {
            if (editableName.isEmpty()) editableName = dataBook.name
            if (editableContent.isEmpty()) editableContent = dataBook.content
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
                    // 右上角"保存"按钮：同时保存手册名称和内容
                    TextButton(
                        onClick = {
                            dataBook?.let { book ->
                                val nameToSave = editableName.trim().ifEmpty { book.name }
                                viewModel.updateDataBookNameAndContent(
                                    book.id,
                                    nameToSave,
                                    editableContent
                                ) {
                                    saveMessage = "保存成功"
                                }
                            }
                        },
                        enabled = dataBook != null && !viewModel.isLoading
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
                AnimatedVisibility(
                    visible = isContentVisible && dataBook != null,
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
                            // 手册名称区域：浅蓝色渐变背景填充整个区域
                            // 高度约 120dp（约为原来的 11/20）
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
                                    value = editableName,
                                    onValueChange = { editableName = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(
                                        fontSize = 24.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                    placeholder = {
                                        Text(
                                            text = "手册名称",
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
                                    text = "手册内容",
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.SemiBold
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // 内容可编辑：左对齐、首行缩进2个字符，直接在此修改
                                // 增加默认最小高度，让内容区域覆盖到屏幕底部约 2cm（约 60dp）
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

                            Spacer(modifier = Modifier.height(32.dp))
                        }
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

            if (viewModel.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
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
