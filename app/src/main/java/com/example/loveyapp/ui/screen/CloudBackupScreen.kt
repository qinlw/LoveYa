package com.example.loveyapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loveyapp.ui.viewmodel.CloudBackupViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudBackupScreen(
    onNavigateBack: () -> Unit,
    onDataReload: () -> Unit = {},
    onRestartApp: () -> Unit = {},
    viewModel: CloudBackupViewModel = hiltViewModel()
) {
    val uiState = viewModel.state

    var repoUrl by remember { mutableStateOf("") }
    var token by remember { mutableStateOf("") }
    var tokenVisible by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showUnbindDialog by remember { mutableStateOf(false) }
    var showRestartDialog by remember { mutableStateOf(false) }

    // 云还原成功后关闭旧数据库连接并提示重启
    LaunchedEffect(viewModel.restoreEventTick) {
        if (viewModel.restoreEventTick > 0L) {
            onDataReload()
            showRestartDialog = true
        }
    }

    // 绑定成功后清空输入框（令牌已加密保存，无需保留在输入框）
    LaunchedEffect(uiState.message) {
        if (uiState.message == "绑定成功") {
            repoUrl = ""
            token = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("云备份", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // 绑定配置卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gitee 仓库绑定",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = repoUrl,
                        onValueChange = { repoUrl = it },
                        label = { Text("仓库链接") },
                        placeholder = { Text("https://gitee.com/user/repo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = token,
                        onValueChange = { token = it },
                        label = { Text("私人令牌") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading,
                        visualTransformation = if (tokenVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { tokenVisible = !tokenVisible }) {
                                Icon(
                                    if (tokenVisible) Icons.Filled.VisibilityOff
                                    else Icons.Filled.Visibility,
                                    contentDescription = if (tokenVisible) "隐藏令牌" else "显示令牌"
                                )
                            }
                        }
                    )

                    Text(
                        text = "在 Gitee → 设置 → 私人令牌 生成，权限勾选 projects",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 6.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.bind(repoUrl, token)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        enabled = !uiState.isLoading,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                    ) {
                        Text("绑定仓库")
                    }
                }
            }

            // 已绑定状态卡片
            if (uiState.isBound) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "已绑定：${uiState.repoDisplayName}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "默认分支：${uiState.defaultBranch}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        uiState.lastBackupTime?.let { ts ->
                            Text(
                                text = "上次云备份：${formatTime(ts)}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        uiState.lastRestoreTime?.let { ts ->
                            Text(
                                text = "上次云还原：${formatTime(ts)}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        HorizontalDivider(Modifier.padding(vertical = 12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { viewModel.uploadBackup() },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isLoading,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                            ) {
                                Text("云备份")
                            }
                            OutlinedButton(
                                onClick = { showRestoreDialog = true },
                                modifier = Modifier.weight(1f),
                                enabled = !uiState.isLoading,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                            ) {
                                Text("云还原")
                            }
                        }

                        Button(
                            onClick = { showUnbindDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            enabled = !uiState.isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                        ) {
                            Text("解除绑定")
                        }

                        Text(
                            text = "云还原将覆盖当前用户本地数据",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // 加载指示
            if (uiState.isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        modifier = Modifier.height(24.dp)
                    )
                    Text(
                        text = "处理中…",
                        modifier = Modifier.padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // 消息展示（可复制，便于提取错误信息）
            uiState.message?.let { msg ->
                SelectionContainer {
                    Text(
                        text = msg,
                        color = if (uiState.isError) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // 云还原二次确认
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("云还原确认") },
            text = { Text("云还原会用云端数据覆盖当前用户本地数据，确定继续吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreDialog = false
                    viewModel.downloadBackup()
                }) {
                    Text("确定还原")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 解除绑定二次确认
    if (showUnbindDialog) {
        AlertDialog(
            onDismissRequest = { showUnbindDialog = false },
            title = { Text("解除绑定") },
            text = { Text("解除绑定将清除本地保存的仓库链接与令牌，令牌不可恢复。确定继续吗？") },
            confirmButton = {
                TextButton(onClick = {
                    showUnbindDialog = false
                    viewModel.unbind()
                    repoUrl = ""
                    token = ""
                }) {
                    Text("确定解绑")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnbindDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("云还原成功") },
            text = { Text("云还原可能不会立即生效，是否重启软件重新加载？") },
            confirmButton = {
                TextButton(onClick = {
                    showRestartDialog = false
                    onRestartApp()
                }) {
                    Text("是")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text("否")
                }
            }
        )
    }
}

private fun formatTime(timestamp: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}
