package com.example.loveyapp.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.navigation.NavController
import com.example.loveyapp.ui.component.GenderSelector
import com.example.loveyapp.ui.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onNavigateToUserList: () -> Unit,
    navController: NavController,
    onExportData: ((Boolean) -> Unit) -> Unit = {},
    onImportData: ((Boolean) -> Unit) -> Unit = {},
    onSelectStoragePath: () -> Unit = {},
    onStoragePathChanged: () -> Unit = {},
    onDataReload: () -> Unit = {},
    padding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues()
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val scrollState = rememberScrollState()

    var userBirthday by remember { mutableStateOf("") }
    var userGender by remember { mutableStateOf("保密") }
    var loverName by remember { mutableStateOf("") }
    var loverBirthday by remember { mutableStateOf("") }
    var loverGender by remember { mutableStateOf("保密") }
    var anniversaryDate by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }



    androidx.compose.runtime.LaunchedEffect(viewModel.userInfo) {
        viewModel.userInfo?.let { info ->
            userBirthday = info.userBirthday
            userGender = info.userGender.ifEmpty { "保密" }
            loverName = info.loverName
            loverBirthday = info.loverBirthday
            loverGender = info.loverGender.ifEmpty { "保密" }
            anniversaryDate = info.anniversaryDate
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "设置",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "个人信息",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                com.example.loveyapp.ui.component.DatePicker(
                    label = "生日",
                    selectedDate = userBirthday,
                    onDateSelected = { userBirthday = it }
                )

                Text(
                    text = "性别",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
                GenderSelector(
                    selectedGender = userGender,
                    onGenderSelected = { userGender = it }
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "爱人信息",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                OutlinedTextField(
                    value = loverName,
                    onValueChange = { loverName = it },
                    label = { Text("爱人姓名") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                com.example.loveyapp.ui.component.DatePicker(
                    label = "爱人生日",
                    selectedDate = loverBirthday,
                    onDateSelected = { loverBirthday = it }
                )

                Text(
                    text = "爱人性别",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
                GenderSelector(
                    selectedGender = loverGender,
                    onGenderSelected = { loverGender = it }
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "我们的开始",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                com.example.loveyapp.ui.component.DatePicker(
                    label = "开始日期",
                    selectedDate = anniversaryDate,
                    onDateSelected = { anniversaryDate = it }
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "数据管理",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Text(
                    text = "当前存储路径",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = viewModel.currentStoragePath,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Button(
                    onClick = {
                        onSelectStoragePath()
                        viewModel.refreshStoragePath()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    Text("选择存储路径")
                }

                Button(
                    onClick = {
                        onExportData { success ->
                            message = if (success) "导出成功" else "导出失败"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    Text("导出数据")
                }

                Button(
                    onClick = {
                        onImportData { success ->
                            message = if (success) "导入成功" else "导入失败"
                            if (success) {
                                onDataReload()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    Text("导入数据")
                }

                Button(
                    onClick = {
                        onExportData { success ->
                            message = if (success) "备份成功" else "备份失败"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    Text("备份数据")
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "用户管理",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Button(
                    onClick = onNavigateToUserList,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    Text("切换用户")
                }
            }
        }

        viewModel.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        message?.let { msg ->
            Text(
                text = msg,
                color = if (msg.contains("成功")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        Button(
            onClick = {
                viewModel.updateUserInfo(
                    userBirthday,
                    userGender,
                    loverName,
                    loverBirthday,
                    loverGender,
                    anniversaryDate
                ) {
                    message = "保存成功"
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            enabled = !viewModel.isLoading
        ) {
            Text("保存")
        }

        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Text("退出登录")
        }
    }
}