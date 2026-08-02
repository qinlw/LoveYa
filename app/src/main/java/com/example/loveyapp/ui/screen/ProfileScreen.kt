package com.example.loveyapp.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.loveyapp.ui.navigation.NavRoutes
import com.example.loveyapp.ui.viewmodel.AnniversaryViewModel
import com.example.loveyapp.ui.viewmodel.SettingsViewModel
import com.example.loveyapp.util.LunarCalendarUtil
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun ProfileScreen(
    navController: NavController,
    onLogout: () -> Unit,
    onNavigateToUserList: () -> Unit,
    padding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues()
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val anniversaryViewModel: AnniversaryViewModel = hiltViewModel()
    val scrollState = rememberScrollState()

    var loveDays by remember { mutableStateOf(0) }
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")

    LaunchedEffect(Unit) {
        settingsViewModel.loadUserInfo()
        anniversaryViewModel.loadAnniversaries()
    }

    LaunchedEffect(settingsViewModel.userInfo) {
        settingsViewModel.userInfo?.anniversaryDate?.let { anniversaryDate ->
            if (anniversaryDate.isNotBlank()) {
                try {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val date = LocalDate.parse(anniversaryDate, formatter)
                    loveDays = ChronoUnit.DAYS.between(date, today).toInt().coerceAtLeast(0)
                } catch (e: Exception) {
                    loveDays = 0
                }
            } else {
                loveDays = 0
            }
        }
    }

    val username = settingsViewModel.userInfo?.username ?: ""
    val myName = settingsViewModel.userInfo?.myName ?: ""
    // 优先显示 myName，若空则显示 username
    val displayName = myName.ifBlank { username }
    val userBirthday = settingsViewModel.userInfo?.userBirthday ?: ""
    val userGender = settingsViewModel.userInfo?.userGender?.ifEmpty { "保密" } ?: "保密"
    val loverName = settingsViewModel.userInfo?.loverName ?: ""
    val loverBirthday = settingsViewModel.userInfo?.loverBirthday ?: ""
    val loverGender = settingsViewModel.userInfo?.loverGender?.ifEmpty { "保密" } ?: "保密"
    val anniversaryDate = settingsViewModel.userInfo?.anniversaryDate ?: ""

    fun formatDate(raw: String, label: String): String {
        if (raw.isBlank()) return "$label：- "
        return try {
            val ld = LocalDate.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            "$label：${ld.format(dateFormatter)}（${LunarCalendarUtil.solarToLunar(ld)}）"
        } catch (_: Exception) {
            "$label：$raw"
        }
    }

    val gradientColors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(scrollState)
    ) {
        // 顶部：标题 + 齿轮
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "我的",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            IconButton(
                onClick = { navController.navigate(NavRoutes.Settings.route) },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 恋爱天数卡片
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(gradientColors))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "恋爱天数",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = "$loveDays",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "天",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                    if (anniversaryDate.isNotBlank()) {
                        Text(
                            text = "我们的开始：${formatDate(anniversaryDate, "").replace("：", "")}",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }

        // 用户头像 + 爱人头像
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.take(1).ifBlank { "我" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = displayName.ifBlank { "我" },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = userGender,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                text = "❤",
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFF06292),
                                    Color(0xFFF06292).copy(alpha = 0.5f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = loverName.take(1).ifBlank { "Ta" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = loverName.ifBlank { "爱人" },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = loverGender,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // 生日信息
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 标题单独一行
                Text(
                    text = "我的生日",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // 公历日期单独一行
                val mySolar = if (userBirthday.isNotBlank()) {
                    try {
                        LocalDate.parse(userBirthday, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            .format(dateFormatter)
                    } catch (_: Exception) {
                        userBirthday
                    }
                } else ""
                // 农历日期单独一行
                val myLunar = if (userBirthday.isNotBlank()) {
                    try {
                        LunarCalendarUtil.solarToLunar(
                            LocalDate.parse(userBirthday, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        )
                    } catch (_: Exception) {
                        ""
                    }
                } else ""
                if (mySolar.isBlank()) {
                    Text(
                        text = "未设置",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else {
                    Text(
                        text = mySolar,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    if (myLunar.isNotBlank()) {
                        Text(
                            text = myLunar,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 标题单独一行
                Text(
                    text = "爱人生日",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // 公历日期单独一行
                val loverSolar = if (loverBirthday.isNotBlank()) {
                    try {
                        LocalDate.parse(loverBirthday, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            .format(dateFormatter)
                    } catch (_: Exception) {
                        loverBirthday
                    }
                } else ""
                // 农历日期单独一行
                val loverLunar = if (loverBirthday.isNotBlank()) {
                    try {
                        LunarCalendarUtil.solarToLunar(
                            LocalDate.parse(loverBirthday, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                        )
                    } catch (_: Exception) {
                        ""
                    }
                } else ""
                if (loverSolar.isBlank()) {
                    Text(
                        text = "未设置",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                } else {
                    Text(
                        text = loverSolar,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                    if (loverLunar.isNotBlank()) {
                        Text(
                            text = loverLunar,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // 纪念日数量
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clickable {
                    // 点击跳转到首页让用户添加纪念日
                    navController.navigate(NavRoutes.Home.route)
                }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "纪念日",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${anniversaryViewModel.anniversaries.size} 个",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }
        }

        // 退出登录 / 切换用户
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.material3.OutlinedButton(
                onClick = onNavigateToUserList,
                modifier = Modifier.weight(1f)
            ) {
                Text("切换用户")
            }
            androidx.compose.material3.Button(
                onClick = onLogout,
                modifier = Modifier.weight(1f),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("退出登录")
            }
        }
    }
}
