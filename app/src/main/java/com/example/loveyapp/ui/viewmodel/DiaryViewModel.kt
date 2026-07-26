package com.example.loveyapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveyapp.data.local.entity.Diary
import com.example.loveyapp.data.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

enum class SortOrder {
    ASC,
    DESC
}

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository
) : ViewModel() {
    var diaries by mutableStateOf<List<Diary>>(emptyList())
    var filteredDiaries by mutableStateOf<List<Diary>>(emptyList())
    var currentDiary by mutableStateOf<Diary?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    private var _searchQuery by mutableStateOf("")
    private var _selectedTag by mutableStateOf<String?>(null)
    var sortOrder by mutableStateOf(SortOrder.DESC)
    val searchQuery get() = _searchQuery
    val selectedTag get() = _selectedTag

    init {
        loadDiaries()
    }

    fun loadDiaries() {
        viewModelScope.launch {
            isLoading = true
            error = null
            diaries = diaryRepository.getAllDiaries()
            applyFilters()
            isLoading = false
        }
    }

    fun applyFilters() {
        var result = diaries

        if (_searchQuery.isNotBlank()) {
            val query = _searchQuery.lowercase()
            result = result.filter {
                it.notebookName.lowercase().contains(query) ||
                it.content.lowercase().contains(query) ||
                it.tags.lowercase().contains(query)
            }
        }

        if (_selectedTag != null) {
            result = result.filter { it.tags.contains(_selectedTag!!) }
        }

        result = if (sortOrder == SortOrder.DESC) {
            result.sortedByDescending { it.createdAt }
        } else {
            result.sortedBy { it.createdAt }
        }

        filteredDiaries = result
    }

    fun getDiaryById(id: Long) {
        viewModelScope.launch {
            isLoading = true
            error = null
            currentDiary = diaryRepository.getDiaryById(id)
            isLoading = false
        }
    }

    fun addDiary(notebookName: String, content: String, tags: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val success = diaryRepository.addDiary(
                Diary(
                    notebookName = notebookName,
                    content = content,
                    date = today,
                    tags = tags
                )
            ) != null
            if (success) {
                loadDiaries()
                onSuccess()
            } else {
                error = "添加失败"
            }
            isLoading = false
        }
    }

    fun updateDiary(id: Long, notebookName: String, content: String, tags: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val existingDiary = diaryRepository.getDiaryById(id)
            if (existingDiary != null) {
                val success = diaryRepository.updateDiary(
                    existingDiary.copy(
                        notebookName = notebookName,
                        content = content,
                        tags = tags
                    )
                )
                if (success) {
                    loadDiaries()
                    onSuccess()
                } else {
                    error = "更新失败"
                }
            } else {
                error = "日记不存在"
            }
            isLoading = false
        }
    }

    fun deleteDiary(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val success = diaryRepository.deleteDiary(id)
            if (success) {
                loadDiaries()
                onSuccess()
            } else {
                error = "删除失败"
            }
            isLoading = false
        }
    }

    fun deleteDiaries(ids: List<Long>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val success = diaryRepository.deleteDiaries(ids)
            if (success) {
                loadDiaries()
                onSuccess()
            } else {
                error = "删除失败"
            }
            isLoading = false
        }
    }

    fun getTags(): List<String> {
        return diaries.flatMap { it.tags.split(",").map { tag -> tag.trim() } }
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

    fun toggleSortOrder() {
        sortOrder = if (sortOrder == SortOrder.ASC) SortOrder.DESC else SortOrder.ASC
        applyFilters()
    }
}
