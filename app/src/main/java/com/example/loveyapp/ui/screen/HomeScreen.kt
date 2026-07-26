package com.example.loveyapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.loveyapp.ui.component.AddAnniversaryDialog
import com.example.loveyapp.ui.component.AnniversaryCard
import com.example.loveyapp.ui.viewmodel.AnniversaryViewModel
import com.example.loveyapp.ui.viewmodel.SettingsViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun HomeScreen(
    onNavigateToAddAnniversary: () -> Unit,
    navController: NavController,
    padding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues(),
    onFabClick: () -> Unit = {}
) {
    val anniversaryViewModel: AnniversaryViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingAnniversary by remember { mutableStateOf<com.example.loveyapp.ui.viewmodel.AnniversaryWithDays?>(null) }
    var loveDays by remember { mutableStateOf(0) }
    val today = LocalDate.now()
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE")

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

    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = today.format(dateFormatter),
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = today.format(dayOfWeekFormatter),
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Text(
                text = "恋爱天数",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 24.dp)
            )
            Text(
                text = loveDays.toString(),
                fontSize = 48.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = "天",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Text(
            text = "即将到来的纪念日",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 16.dp, bottom = 12.dp)
        )

        if (anniversaryViewModel.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "加载中...")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(anniversaryViewModel.anniversaries) { index, item ->
                    AnniversaryCard(
                        item = item,
                        index = index,
                        onEdit = {
                            editingAnniversary = item
                            showEditDialog = true
                        },
                        onDelete = {
                            anniversaryViewModel.deleteAnniversary(item.config.id) {}
                        }
                    )
                }

                if (anniversaryViewModel.anniversaries.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "暂无纪念日",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "点击下方按钮添加",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAnniversaryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, date, calendarType ->
                anniversaryViewModel.addAnniversary(name, date, calendarType) {
                    showAddDialog = false
                }
            }
        )
    }

    if (showEditDialog && editingAnniversary != null) {
        AddAnniversaryDialog(
            onDismiss = {
                showEditDialog = false
                editingAnniversary = null
            },
            onSave = { name, date, calendarType ->
                editingAnniversary?.let { item ->
                    anniversaryViewModel.updateAnniversary(item.config.id, name, date, calendarType) {
                        showEditDialog = false
                        editingAnniversary = null
                    }
                }
            },
            initialName = editingAnniversary!!.config.name,
            initialDate = editingAnniversary!!.config.targetDate,
            initialCalendarType = editingAnniversary!!.config.calendarType
        )
    }
}
