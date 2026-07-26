package com.example.loveyapp.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.loveyapp.ui.viewmodel.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
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
    val searchInput = remember { mutableStateOf("") }

    LaunchedEffect(backStackEntry.value) {
        viewModel.loadDiaries()
        selectedIds.value = emptySet()
    }

    LaunchedEffect(searchInput.value) {
        viewModel.setSearchQuery(searchInput.value)
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
            } else {
                IconButton(onClick = { viewModel.toggleSortOrder() }) {
                    Icon(
                        imageVector = if (viewModel.sortOrder == SortOrder.DESC) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = if (viewModel.sortOrder == SortOrder.DESC) "降序" else "升序",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        OutlinedTextField(
            value = searchInput.value,
            onValueChange = { searchInput.value = it },
            label = { Text("搜索日记") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            singleLine = true
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, end = 16.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = viewModel.selectedTag == null,
                onClick = { viewModel.setSelectedTag(null) },
                label = { Text("全部") }
            )
            val tags = viewModel.getTags()
            for (index in tags.indices) {
                val tag = tags[index]
                FilterChip(
                    selected = viewModel.selectedTag == tag,
                    onClick = { viewModel.setSelectedTag(tag) },
                    label = { Text(tag) }
                )
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
                    items = viewModel.filteredDiaries,
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

                if (viewModel.filteredDiaries.isEmpty()) {
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
