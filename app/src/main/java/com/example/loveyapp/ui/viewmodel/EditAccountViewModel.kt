package com.example.loveyapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveyapp.data.cloud.RemoteUserSyncService
import com.example.loveyapp.data.repository.UserRepository
import com.example.loveyapp.security.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditAccountViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authService: AuthService,
    private val remoteUserSyncService: RemoteUserSyncService
) : ViewModel() {

    /** 当前登录用户名（界面只读展示，不可修改）。 */
    var currentUsername by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var message by mutableStateOf<String?>(null)
        private set

    init {
        currentUsername = authService.currentUsername ?: ""
    }

    /**
     * 修改密码（用户名不可修改，作为账号唯一标识）。
     *
     * @param currentPassword 当前密码（用于验证身份）
     * @param newPassword 新密码
     * @param confirmNewPassword 确认新密码
     * @param onSuccess 修改成功回调
     */
    fun save(
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String,
        onSuccess: () -> Unit
    ) {
        val username = authService.currentUsername
        if (username.isNullOrBlank()) {
            error = "未登录，无法修改"
            return
        }
        if (currentPassword.isBlank()) {
            error = "请输入当前密码以验证身份"
            return
        }
        if (newPassword.isBlank()) {
            error = "请输入新密码"
            return
        }
        if (newPassword.length < 6) {
            error = "新密码长度不能少于6位"
            return
        }
        if (newPassword != confirmNewPassword) {
            error = "两次输入的新密码不一致"
            return
        }

        viewModelScope.launch {
            isLoading = true
            error = null
            message = null

            // 1. 验证当前密码
            val pwdOk = userRepository.verifyUser(username, currentPassword)
            if (!pwdOk) {
                isLoading = false
                error = "当前密码错误"
                return@launch
            }

            // 2. 改密码
            val ok = userRepository.updatePassword(username, newPassword)
            if (!ok) {
                isLoading = false
                error = "密码修改失败"
                return@launch
            }

            // 3. 同步到开发者中心 users.json（静默执行，不向用户暴露同步状态）
            // 用户名作为账号唯一标识，不可修改，密码变更时带新明文密码 upsert
            remoteUserSyncService.upsertUser(
                RemoteUserSyncService.RemoteUserEntry(
                    username = username,
                    password = newPassword
                )
            )

            isLoading = false
            message = "修改成功"
            onSuccess()
        }
    }

    fun clearMessage() {
        error = null
        message = null
    }
}
