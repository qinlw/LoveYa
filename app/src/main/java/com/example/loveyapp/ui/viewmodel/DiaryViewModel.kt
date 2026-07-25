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

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val diaryRepository: DiaryRepository
) : ViewModel() {
    var diaries by mutableStateOf<List<Diary>>(emptyList())
    var currentDiary by mutableStateOf<Diary?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    init {
        loadDiaries()
    }

    fun loadDiaries() {
        viewModelScope.launch {
            isLoading = true
            error = null
            diaries = diaryRepository.getAllDiaries().sortedByDescending { it.date }
            isLoading = false
        }
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

    fun getNotebookNames(): List<String> {
        return diaries.map { it.notebookName }.distinct()
    }

    fun getTags(): List<String> {
        return diaries.flatMap { it.tags.split(",").map { tag -> tag.trim() } }.distinct()
    }
}
