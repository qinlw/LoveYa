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
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
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
import com.example.loveyapp.ui.component.AttributeCard
import com.example.loveyapp.ui.viewmodel.DataBookSortType
import com.example.loveyapp.ui.viewmodel.DataBookViewModel
import com.example.loveyapp.ui.viewmodel.SortOrder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataBookListScreen(
    onNavigateToDetail: (Long) -> Unit,
    onNavigateToEdit: (Long?) -> Unit,
    navController: NavController,
    padding: androidx.compose.foundation.layout.PaddingValues = androidx.compose.foundation.layout.PaddingValues()
) {
    val viewModel: DataBookViewModel = hiltViewModel()
    val backStackEntry = navController.currentBackStackEntryAsState()
    val selectedIds = remember { mutableStateOf<Set<Long>>(emptySet()) }
    val searchInput = remember { mutableStateOf("") }
    val showSortMenu = remember { mutableStateOf(false) }

    LaunchedEffect(backStackEntry.value) {
        viewModel.loadDataBooks()
        selectedIds.value = emptySet()
    }

    LaunchedEffect(searchInput.value) {
        viewModel.setSearchQuery(searchInput.value)
    }

    val sortButtonText = when (viewModel.sortType) {
        DataBookSortType.DEFAULT -> "默认"
        DataBookSortType.TIME -> "时间"
        DataBookSortType.NAME -> "字母"
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
                text = "数据手册",
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
                    Button(
                        onClick = {
                            viewModel.deleteDataBooks(selectedIds.value.toList()) {
                                selectedIds.value = emptySet()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("删除")
                    }
                    TextButton(
                        onClick = { selectedIds.value = emptySet() }
                    ) {
                        Text("取消")
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { showSortMenu.value = true },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(sortButtonText)
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "展开",
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            DropdownMenu(
                expanded = showSortMenu.value,
                onDismissRequest = { showSortMenu.value = false }
            ) {
                DropdownMenuItem(
                    text = { Text("默认") },
                    onClick = {
                        viewModel.updateSortType(DataBookSortType.DEFAULT)
                        showSortMenu.value = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("时间") },
                    onClick = {
                        viewModel.updateSortType(DataBookSortType.TIME)
                        showSortMenu.value = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("字母") },
                    onClick = {
                        viewModel.updateSortType(DataBookSortType.NAME)
                        showSortMenu.value = false
                    }
                )
            }

            IconButton(onClick = { viewModel.toggleSortOrder() }) {
                Icon(
                    imageVector = if (viewModel.sortOrder == SortOrder.DESC) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = if (viewModel.sortOrder == SortOrder.DESC) "降序" else "升序",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        OutlinedTextField(
            value = searchInput.value,
            onValueChange = { searchInput.value = it },
            label = { Text("搜索手册") },
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
                    items = viewModel.filteredDataBooks,
                    key = { _, dataBook -> dataBook.id }
                ) { index, dataBook ->
                    AttributeCard(
                        dataBook = dataBook,
                        index = index,
                        isSelected = selectedIds.value.contains(dataBook.id),
                        onClick = {
                            if (selectedIds.value.isNotEmpty()) {
                                val newSelection = if (selectedIds.value.contains(dataBook.id)) {
                                    selectedIds.value.minus(dataBook.id)
                                } else {
                                    selectedIds.value.plus(dataBook.id)
                                }
                                selectedIds.value = newSelection
                            } else {
                                onNavigateToDetail(dataBook.id)
                            }
                        },
                        onLongClick = {
                            if (selectedIds.value.isEmpty()) {
                                selectedIds.value = setOf(dataBook.id)
                            }
                        }
                    )
                }

                if (viewModel.filteredDataBooks.isEmpty()) {
                    item(key = "empty_state") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "暂无数据",
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
