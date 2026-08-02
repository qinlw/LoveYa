package com.example.loveyapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveyapp.data.local.entity.UserInfo
import com.example.loveyapp.data.prefs.DisplaySettingsStore
import com.example.loveyapp.data.repository.UserRepository
import com.example.loveyapp.security.AuthService
import com.example.loveyapp.util.DisplayCalendarMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authService: AuthService,
    private val dataBackupManager: com.example.loveyapp.data.service.DataBackupManager,
    private val displaySettingsStore: DisplaySettingsStore
) : ViewModel() {
    var userInfo by mutableStateOf<UserInfo?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var currentStoragePath by mutableStateOf("")
    var displayCalendarMode by mutableStateOf(DisplayCalendarMode.BOTH)

    init {
        loadUserInfo()
        loadDisplayCalendarMode()
    }

    private fun loadDisplayCalendarMode() {
        displayCalendarMode = displaySettingsStore.getDisplayCalendarMode()
    }

    fun saveDisplayCalendarMode(mode: DisplayCalendarMode) {
        displaySettingsStore.saveDisplayCalendarMode(mode)
        displayCalendarMode = mode
    }

    fun loadUserInfo() {
        viewModelScope.launch {
            isLoading = true
            error = null
            val username = authService.currentUsername
            if (username != null) {
                userInfo = userRepository.getUserInfo(username)
            }
            currentStoragePath = dataBackupManager.getDefaultBackupPath()
            isLoading = false
        }
    }

    fun refreshStoragePath() {
        currentStoragePath = dataBackupManager.getDefaultBackupPath()
    }

    fun updateUserInfo(
        myName: String,
        userBirthday: String,
        userGender: String,
        loverName: String,
        loverBirthday: String,
        loverGender: String,
        anniversaryDate: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isLoading = true
            error = null
            val username = authService.currentUsername
            if (username != null && userInfo != null) {
                val updatedUserInfo = userInfo!!.copy(
                    myName = myName,
                    userBirthday = userBirthday,
                    userGender = userGender,
                    loverName = loverName,
                    loverBirthday = loverBirthday,
                    loverGender = loverGender,
                    anniversaryDate = anniversaryDate
                )
                val success = userRepository.updateUserInfo(username, updatedUserInfo)
                if (success) {
                    userInfo = updatedUserInfo
                    onSuccess()
                } else {
                    error = "更新失败"
                }
            } else {
                error = "用户信息不存在"
            }
            isLoading = false
        }
    }
}
