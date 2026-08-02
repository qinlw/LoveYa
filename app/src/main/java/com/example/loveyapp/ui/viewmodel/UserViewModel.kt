package com.example.loveyapp.ui.viewmodel

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveyapp.data.cloud.RemoteUserSyncService
import com.example.loveyapp.data.repository.UserRepository
import com.example.loveyapp.security.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authService: AuthService,
    private val remoteUserSyncService: RemoteUserSyncService,
    @ApplicationContext private val context: Context
) : ViewModel() {
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var isLoggedIn by mutableStateOf(false)

    init {
        isLoggedIn = authService.isLoggedIn
    }

    // 检查网络连接是否可用（注册/登录必须联网，以保证 users.json 能同步到云端）
    private fun isNetworkAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nc = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
               nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
               nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null

            if (!isNetworkAvailable()) {
                error = "登录需要联网，请检查网络连接后重试"
                isLoading = false
                return@launch
            }

            if (username.isBlank() || password.isBlank()) {
                error = "用户名和密码不能为空"
                isLoading = false
                return@launch
            }

            if (password.length < 6) {
                error = "密码长度不能少于6位"
                isLoading = false
                return@launch
            }

            val success = userRepository.login(username, password)
            if (success) {
                authService.login(username)
                isLoggedIn = true
                // 登录成功后同步明文密码到 users.json
                // 老用户云端可能没有密码记录，登录时补传
                remoteUserSyncService.upsertUser(
                    RemoteUserSyncService.RemoteUserEntry(
                        username = username,
                        password = password
                    )
                )
                onSuccess()
            } else {
                error = "用户名或密码错误"
            }
            isLoading = false
        }
    }

    fun register(username: String, password: String, confirmPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null

            if (!isNetworkAvailable()) {
                error = "注册需要联网，请检查网络连接后重试"
                isLoading = false
                return@launch
            }

            if (username.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                error = "所有字段不能为空"
                isLoading = false
                return@launch
            }

            if (password.length < 6) {
                error = "密码长度不能少于6位"
                isLoading = false
                return@launch
            }

            if (password != confirmPassword) {
                error = "两次输入的密码不一致"
                isLoading = false
                return@launch
            }

            // 先占用用户名到 usersID.json（防同名，全局唯一），再本地建库
            // 这样网络异常或用户名已被占用时，本地不会建库，避免脏数据
            val reserved = remoteUserSyncService.checkAndReserveUserId(username)
            if (!reserved) {
                error = "该用户名已被占用或网络异常，请更换用户名或检查网络"
                isLoading = false
                return@launch
            }

            val success = userRepository.register(username, password)
            if (success) {
                // 同步到开发者中心 users.json，便于多设备异地账号找回
                remoteUserSyncService.upsertUser(
                    RemoteUserSyncService.RemoteUserEntry(
                        username = username,
                        password = password
                    )
                )
                onSuccess()
            } else {
                error = "注册失败，用户名可能已存在"
            }
            isLoading = false
        }
    }

    fun logout() {
        authService.logout()
        isLoggedIn = false
    }
}
