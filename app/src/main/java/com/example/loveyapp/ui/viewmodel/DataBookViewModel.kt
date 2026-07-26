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

enum class DataBookSortType {
    DEFAULT,
    TIME,
    NAME
}

@HiltViewModel
class DataBookViewModel @Inject constructor(
    private val dataBookRepository: DataBookRepository
) : ViewModel() {
    var dataBooks by mutableStateOf<List<DataBook>>(emptyList())
    var filteredDataBooks by mutableStateOf<List<DataBook>>(emptyList())
    var currentDataBook by mutableStateOf<DataBook?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    private var _searchQuery by mutableStateOf("")
    private var _selectedTag by mutableStateOf<String?>(null)
    val searchQuery get() = _searchQuery
    val selectedTag get() = _selectedTag
    var sortType by mutableStateOf(DataBookSortType.DEFAULT)
    var sortOrder by mutableStateOf(SortOrder.DESC)

    init {
        loadDataBooks()
    }

    fun loadDataBooks() {
        viewModelScope.launch {
            isLoading = true
            error = null
            dataBooks = dataBookRepository.getAllDataBooks()
            applyFilters()
            isLoading = false
        }
    }

    fun applyFilters() {
        var result = dataBooks

        if (_searchQuery.isNotBlank()) {
            val query = _searchQuery.lowercase()
            result = result.filter {
                it.name.lowercase().contains(query) ||
                it.content.lowercase().contains(query) ||
                it.tags.lowercase().contains(query)
            }
        }

        if (_selectedTag != null) {
            result = result.filter { it.tags.contains(_selectedTag!!) }
        }

        result = when (sortType) {
            DataBookSortType.DEFAULT -> {
                if (sortOrder == SortOrder.DESC) {
                    result.sortedByDescending { it.createdAt }
                } else {
                    result.sortedBy { it.createdAt }
                }
            }
            DataBookSortType.TIME -> {
                if (sortOrder == SortOrder.DESC) {
                    result.sortedByDescending { it.createdAt }
                } else {
                    result.sortedBy { it.createdAt }
                }
            }
            DataBookSortType.NAME -> {
                if (sortOrder == SortOrder.DESC) {
                    result.sortedByDescending { it.name }
                } else {
                    result.sortedBy { it.name }
                }
            }
        }

        filteredDataBooks = result
    }

    fun updateSortType(type: DataBookSortType) {
        sortType = type
        applyFilters()
    }

    fun toggleSortOrder() {
        sortOrder = if (sortOrder == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC
        applyFilters()
    }

    fun getDataBookById(id: Long) {
        viewModelScope.launch {
            isLoading = true
            error = null
            currentDataBook = dataBookRepository.getDataBookById(id)
            isLoading = false
        }
    }

    fun addDataBook(name: String, content: String, tags: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val success = dataBookRepository.addDataBook(
                DataBook(
                    name = name,
                    content = content,
                    tags = tags
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

    fun updateDataBook(id: Long, name: String, content: String, tags: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val existingDataBook = dataBookRepository.getDataBookById(id)
            if (existingDataBook != null) {
                val success = dataBookRepository.updateDataBook(
                    existingDataBook.copy(
                        name = name,
                        content = content,
                        tags = tags
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

    fun getTags(): List<String> {
        return dataBooks.flatMap { it.tags.split(",").map { tag -> tag.trim() } }
            .filter { it.isNotBlank() }
            .distinct()
    }

    fun setSearchQuery(query: String) {
        _searchQuery = query
        applyFilters()
    }

    fun setSelectedTag(tag: String?) {
        _selectedTag = tag
        applyFilters()
    }
}
