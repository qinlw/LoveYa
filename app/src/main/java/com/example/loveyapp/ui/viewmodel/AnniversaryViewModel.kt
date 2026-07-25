package com.example.loveyapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveyapp.data.local.entity.AnniversaryConfig
import com.example.loveyapp.data.repository.AnniversaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class AnniversaryWithDays(
    val config: AnniversaryConfig,
    val daysRemaining: Int,
    val isExpired: Boolean
)

@HiltViewModel
class AnniversaryViewModel @Inject constructor(
    private val anniversaryRepository: AnniversaryRepository
) : ViewModel() {
    var anniversaries by mutableStateOf<List<AnniversaryWithDays>>(emptyList())
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    init {
        loadAnniversaries()
    }

    fun loadAnniversaries() {
        viewModelScope.launch {
            isLoading = true
            error = null
            val configs = anniversaryRepository.getEnabledAnniversaries()
            anniversaries = configs.map { config ->
                val daysRemaining = calculateDaysRemaining(config.targetDate)
                AnniversaryWithDays(
                    config = config,
                    daysRemaining = daysRemaining,
                    isExpired = daysRemaining < 0
                )
            }.sortedBy { if (it.isExpired) Int.MAX_VALUE else it.daysRemaining }
            isLoading = false
        }
    }

    fun calculateDaysRemaining(targetDate: String): Int {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val target = LocalDate.parse(targetDate, formatter)
            val today = LocalDate.now()
            var remaining = ChronoUnit.DAYS.between(today, target).toInt()

            if (remaining < 0) {
                remaining = ChronoUnit.DAYS.between(today, target.plusYears(1)).toInt()
            }
            remaining
        } catch (e: Exception) {
            0
        }
    }

    fun addAnniversary(name: String, targetDate: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val success = anniversaryRepository.addAnniversary(
                AnniversaryConfig(
                    name = name,
                    targetDate = targetDate,
                    displayOrder = anniversaries.size,
                    enabled = true
                )
            ) != null
            if (success) {
                loadAnniversaries()
                onSuccess()
            } else {
                error = "添加失败"
            }
            isLoading = false
        }
    }

    fun updateAnniversary(id: Long, name: String, targetDate: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val existing = anniversaryRepository.getAnniversaryById(id)
            if (existing != null) {
                val updated = existing.copy(
                    name = name,
                    targetDate = targetDate,
                    updatedAt = System.currentTimeMillis()
                )
                val success = anniversaryRepository.updateAnniversary(updated)
                if (success) {
                    loadAnniversaries()
                    onSuccess()
                } else {
                    error = "更新失败"
                }
            } else {
                error = "纪念日不存在"
            }
            isLoading = false
        }
    }

    fun deleteAnniversary(id: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val success = anniversaryRepository.deleteAnniversary(id)
            if (success) {
                loadAnniversaries()
                onSuccess()
            } else {
                error = "删除失败"
            }
            isLoading = false
        }
    }
}
