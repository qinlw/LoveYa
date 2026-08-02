package com.example.loveyapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loveyapp.BuildConfig
import com.example.loveyapp.config.DeveloperConfig

/**
 * 开发者模式界面。
 *
 * 入口：设置界面底部"开发者"按钮，点击后需输入开发者密码（= DEV_GITEE_TOKEN）验证通过方可进入。
 * 进入后显示当前开发者中心仓库配置（不展示完整令牌）、手动同步当前用户到 users.json 等操作。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(onNavigateBack: () -> Unit) {
    var authenticated by remember { mutableStateOf(false) }
    var pwdInput by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("开发者模式") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!authenticated) {
                // 密码验证
                Text(
                    text = "请输入开发者密码",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = pwdInput,
                    onValueChange = {
                        pwdInput = it
                        authError = null
                    },
                    label = { Text("开发者密码") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = authError != null,
                    supportingText = {
                        authError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = {
                        // 密码即 token.properties 中的 DEV_GITEE_TOKEN
                        if (pwdInput == BuildConfig.DEV_GITEE_TOKEN) {
                            authenticated = true
                            authError = null
                        } else {
                            authError = "密码错误"
                        }
                    },
                    enabled = pwdInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("验证")
                }
            } else {
                // 已进入开发者模式：展示配置信息
                DeveloperInfoCard()
            }
        }
    }
}

@Composable
private fun DeveloperInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "开发者中心仓库配置",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            ConfigRow("仓库地址", DeveloperConfig.repoUrl)
            ConfigRow("分支", DeveloperConfig.branch)
            ConfigRow("用户数据文件", DeveloperConfig.userDataFilePath)
            ConfigRow("用户ID文件", DeveloperConfig.userIdFilePath)
            ConfigRow(
                "访问令牌",
                if (BuildConfig.DEV_GITEE_TOKEN.isNotEmpty()) "已配置（隐藏）" else "未配置"
            )
        }
    }
}

@Composable
private fun ConfigRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
