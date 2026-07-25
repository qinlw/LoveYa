package com.example.loveyapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.loveyapp.ui.component.DiaryCard
import com.example.loveyapp.ui.viewmodel.DiaryViewModel

@Composable
fun DiaryListScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToEdit: (Long?) -> Unit,
    navController: NavController,
    padding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues()
) {
    val viewModel: DiaryViewModel = hiltViewModel()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val selectedIds = remember { mutableStateOf<Set<Long>>(emptySet()) }

    LaunchedEffect(backStackEntry.value) {
        viewModel.loadDiaries()
        selectedIds.value = emptySet()
    }

    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "日记",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (selectedIds.value.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "已选 ${selectedIds.value.size} 项",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    androidx.compose.material3.Button(
                        onClick = {
                            viewModel.deleteDiaries(selectedIds.value.toList()) {
                                selectedIds.value = emptySet()
                            }
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("删除")
                    }
                    androidx.compose.material3.TextButton(
                        onClick = { selectedIds.value = emptySet() }
                    ) {
                        Text("取消")
                    }
                }
            }
        }

        if (viewModel.isLoading) {
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
                itemsIndexed(
                    items = viewModel.diaries,
                    key = { _, diary -> diary.id }
                ) { index, diary ->
                    DiaryCard(
                        diary = diary,
                        index = index,
                        isSelected = selectedIds.value.contains(diary.id),
                        onClick = {
                            if (selectedIds.value.isNotEmpty()) {
                                val newSelection = if (selectedIds.value.contains(diary.id)) {
                                    selectedIds.value.minus(diary.id)
                                } else {
                                    selectedIds.value.plus(diary.id)
                                }
                                selectedIds.value = newSelection
                            } else {
                                onNavigateToDetail(diary.id)
                            }
                        },
                        onLongClick = {
                            if (selectedIds.value.isEmpty()) {
                                selectedIds.value = setOf(diary.id)
                            }
                        }
                    )
                }

                if (viewModel.diaries.isEmpty()) {
                    item(key = "empty_state") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "暂无日记",
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
}