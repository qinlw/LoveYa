package com.example.loveyapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveyapp.data.local.entity.UserInfo
import com.example.loveyapp.data.prefs.BirthdaySettingsStore
import com.example.loveyapp.data.prefs.DisplaySettingsStore
import com.example.loveyapp.data.repository.UserRepository
import com.example.loveyapp.security.AuthService
import com.example.loveyapp.util.CalendarType
import com.example.loveyapp.util.DisplayCalendarMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authService: AuthService,
    private val dataBackupManager: com.example.loveyapp.data.service.DataBackupManager,
    private val displaySettingsStore: DisplaySettingsStore,
    private val birthdaySettingsStore: BirthdaySettingsStore
) : ViewModel() {
    var userInfo by mutableStateOf<UserInfo?>(null)
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var currentStoragePath by mutableStateOf("")
    var displayCalendarMode by mutableStateOf(DisplayCalendarMode.BOTH)
    var myBirthdayCalendarType by mutableStateOf(CalendarType.SOLAR)
    var loverBirthdayCalendarType by mutableStateOf(CalendarType.SOLAR)
    var anniversaryCalendarType by mutableStateOf(CalendarType.SOLAR)

    init {
        loadUserInfo()
        loadDisplayCalendarMode()
        loadBirthdayCalendarTypes()
    }

    private fun loadDisplayCalendarMode() {
        displayCalendarMode = displaySettingsStore.getDisplayCalendarMode()
    }

    private fun loadBirthdayCalendarTypes() {
        myBirthdayCalendarType = try { birthdaySettingsStore.getMyBirthdayCalendarType() } catch (_: Exception) { CalendarType.SOLAR }
        loverBirthdayCalendarType = try { birthdaySettingsStore.getLoverBirthdayCalendarType() } catch (_: Exception) { CalendarType.SOLAR }
        anniversaryCalendarType = try { birthdaySettingsStore.getAnniversaryCalendarType() } catch (_: Exception) { CalendarType.SOLAR }
    }

    /** 重新从持久化存储加载日历相关设置（供跨页面返回时同步最新值） */
    fun reloadCalendarSettings() {
        loadDisplayCalendarMode()
        loadBirthdayCalendarTypes()
    }

    /** 更新"我的生日"日历类型（公历/农历），立即持久化 */
    fun updateMyBirthdayCalendarType(type: CalendarType) {
        try { birthdaySettingsStore.saveMyBirthdayCalendarType(type) } catch (_: Exception) {}
        myBirthdayCalendarType = type
    }

    /** 更新"爱人生日"日历类型（公历/农历），立即持久化 */
    fun updateLoverBirthdayCalendarType(type: CalendarType) {
        try { birthdaySettingsStore.saveLoverBirthdayCalendarType(type) } catch (_: Exception) {}
        loverBirthdayCalendarType = type
    }

    /** 更新"我们的开始"日历类型（公历/农历），立即持久化 */
    fun updateAnniversaryCalendarType(type: CalendarType) {
        try { birthdaySettingsStore.saveAnniversaryCalendarType(type) } catch (_: Exception) {}
        anniversaryCalendarType = type
    }

    /** 切换"我的生日"日历类型（公历 ↔ 农历） */
    fun toggleMyBirthdayCalendarType() {
        try {
            myBirthdayCalendarType = birthdaySettingsStore.toggleMyBirthdayCalendarType()
        } catch (_: Exception) {}
    }

    /** 切换"爱人生日"日历类型（公历 ↔ 农历） */
    fun toggleLoverBirthdayCalendarType() {
        try {
            loverBirthdayCalendarType = birthdaySettingsStore.toggleLoverBirthdayCalendarType()
        } catch (_: Exception) {}
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
