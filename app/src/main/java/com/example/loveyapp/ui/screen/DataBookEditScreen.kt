package com.example.loveyapp.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import com.example.loveyapp.ui.viewmodel.DataBookViewModel

@Composable
fun DataBookEditScreen(
    dataBookId: Long?,
    onSaveSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: DataBookViewModel = hiltViewModel()

    var notebookName by remember { mutableStateOf("") }
    var attributeName by remember { mutableStateOf("") }
    var attributeValues by remember { mutableStateOf("") }

    if (dataBookId != null && viewModel.currentDataBook == null) {
        viewModel.getDataBookById(dataBookId)
    }

    val dataBook = viewModel.currentDataBook
    if (dataBook != null && notebookName.isEmpty()) {
        notebookName = dataBook.notebookName
        attributeName = dataBook.attributeName
        attributeValues = dataBook.attributeValues
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = if (dataBookId != null) "编辑属性" else "新建属性",
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = notebookName,
                onValueChange = { notebookName = it },
                label = { Text("笔记本名称") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = attributeName,
                onValueChange = { attributeName = it },
                label = { Text("属性名称") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )

            OutlinedTextField(
                value = attributeValues,
                onValueChange = { attributeValues = it },
                label = { Text("属性值（逗号分隔）") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                minLines = 3,
                maxLines = 10
            )

            viewModel.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Button(
                onClick = {
                    if (dataBookId != null) {
                        viewModel.updateDataBook(dataBookId, notebookName, attributeName, attributeValues, onSaveSuccess)
                    } else {
                        viewModel.addDataBook(notebookName, attributeName, attributeValues, onSaveSuccess)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                enabled = !viewModel.isLoading && notebookName.isNotBlank() && attributeName.isNotBlank()
            ) {
                Text(if (dataBookId != null) "保存修改" else "保存")
            }

            Button(
                onClick = onNavigateBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text("取消")
            }
        }
    }
}
