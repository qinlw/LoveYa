package com.example.loveyapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
import com.example.loveyapp.ui.navigation.NavRoutes
import com.example.loveyapp.ui.viewmodel.SettingsViewModel
import com.example.loveyapp.util.DisplayCalendarMode

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onNavigateToUserList: () -> Unit,
    navController: NavController,
    onExportData: ((Boolean) -> Unit) -> Unit = {},
    onImportData: ((Boolean) -> Unit) -> Unit = {},
    onSelectStoragePath: () -> Unit = {},
    onDataReload: () -> Unit = {},
    padding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues()
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    val scrollState = rememberScrollState()

    var userBirthday by remember { mutableStateOf("") }
    var userBirthdayCalendarType by remember { mutableStateOf("SOLAR") }
    var myName by remember { mutableStateOf("") }
    var userGender by remember { mutableStateOf("保密") }
    var loverName by remember { mutableStateOf("") }
    var loverBirthday by remember { mutableStateOf("") }
    var loverBirthdayCalendarType by remember { mutableStateOf("SOLAR") }
    var loverGender by remember { mutableStateOf("保密") }
    var anniversaryDate by remember { mutableStateOf("") }
    var anniversaryCalendarType by remember { mutableStateOf("SOLAR") }
    var message by remember { mutableStateOf<String?>(null) }



    androidx.compose.runtime.LaunchedEffect(viewModel.userInfo) {
        viewModel.userInfo?.let { info ->
            userBirthday = info.userBirthday
            myName = info.myName
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
                    calendarType = userBirthdayCalendarType,
                    onDateSelected = { date, type ->
                        userBirthday = date
                        userBirthdayCalendarType = type
                    }
                )

                OutlinedTextField(
                    value = myName,
                    onValueChange = { myName = it },
                    label = { Text("我的姓名") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    singleLine = true
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
                    calendarType = loverBirthdayCalendarType,
                    onDateSelected = { date, type ->
                        loverBirthday = date
                        loverBirthdayCalendarType = type
                    }
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
                    calendarType = anniversaryCalendarType,
                    onDateSelected = { date, type ->
                        anniversaryDate = date
                        anniversaryCalendarType = type
                    }
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
                    text = "显示偏好",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Text(
                    text = "日期显示方式",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modes = DisplayCalendarMode.values()
                    for (index in modes.indices) {
                        val mode = modes[index]
                        val label = when (mode) {
                            DisplayCalendarMode.SOLAR_ONLY -> "只公历"
                            DisplayCalendarMode.LUNAR_ONLY -> "只农历"
                            DisplayCalendarMode.BOTH -> "两者都"
                        }
                        Button(
                            onClick = { viewModel.saveDisplayCalendarMode(mode) },
                            modifier = Modifier.weight(1f),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (viewModel.displayCalendarMode == mode) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                contentColor = if (viewModel.displayCalendarMode == mode) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        ) {
                            Text(label)
                        }
                    }
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
                    text = "数据管理",
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Button(
                    onClick = { navController.navigate(NavRoutes.CloudBackup.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    Text("云备份")
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

                Button(
                    onClick = { navController.navigate(NavRoutes.EditAccount.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp)
                ) {
                    Text("修改账号")
                }

                // 开发者入口（隐蔽，点击后需输入开发者密码）
                Button(
                    onClick = { navController.navigate(NavRoutes.Developer.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                ) {
                    Text("开发者", fontSize = 12.sp)
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
                    myName,
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