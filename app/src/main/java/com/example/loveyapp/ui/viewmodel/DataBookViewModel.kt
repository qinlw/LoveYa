package com.example.loveyapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveyapp.data.local.entity.DataBook
import com.example.loveyapp.data.repository.DataBookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataBookViewModel @Inject constructor(
    private val dataBookRepository: DataBookRepository
) : ViewModel() {
    var dataBooks by mutableStateOf<List<DataBook>>(emptyList())
    var currentDataBook by mutableStateOf<DataBook?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    init {
        loadDataBooks()
    }

    fun loadDataBooks() {
        viewModelScope.launch {
            isLoading = true
            error = null
            dataBooks = dataBookRepository.getAllDataBooks().sortedBy { it.notebookName }
            isLoading = false
        }
    }

    fun getDataBookById(id: Long) {
        viewModelScope.launch {
            isLoading = true
            error = null
            currentDataBook = dataBookRepository.getDataBookById(id)
            isLoading = false
        }
    }

    fun addDataBook(notebookName: String, attributeName: String, attributeValues: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val success = dataBookRepository.addDataBook(
                DataBook(
                    notebookName = notebookName,
                    attributeName = attributeName,
                    attributeValues = attributeValues
                )
            ) != null
            if (success) {
                loadDataBooks()
                onSuccess()
            } else {
                error = "添加失败"
            }
            isLoading = false
        }
    }

    fun updateDataBook(id: Long, notebookName: String, attributeName: String, attributeValues: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val existingDataBook = dataBookRepository.getDataBookById(id)
            if (existingDataBook != null) {
                val success = dataBookRepository.updateDataBook(
                    existingDataBook.copy(
                        notebookName = notebookName,
                        attributeName = attributeName,
                        attributeValues = attributeValues
                    )
                )
                if (success) {
                    loadDataBooks()
                    onSuccess()
                } else {
                    error = "更新失败"
                }
            } else {
                error = "数据不存在"
            }
            isLoading = false
        }
    }

    fun deleteDataBook(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val success = dataBookRepository.deleteDataBook(id)
            if (success) {
                loadDataBooks()
                onSuccess()
            } else {
                error = "删除失败"
            }
            isLoading = false
        }
    }

    fun deleteDataBooks(ids: List<Long>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val success = dataBookRepository.deleteDataBooks(ids)
            if (success) {
                loadDataBooks()
                onSuccess()
            } else {
                error = "删除失败"
            }
            isLoading = false
        }
    }

    fun getNotebookNames(): List<String> {
        return dataBooks.map { it.notebookName }.distinct()
    }
}
