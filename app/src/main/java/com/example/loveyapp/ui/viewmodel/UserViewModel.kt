package com.example.loveyapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveyapp.data.repository.UserRepository
import com.example.loveyapp.security.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authService: AuthService
) : ViewModel() {
    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var isLoggedIn by mutableStateOf(false)

    init {
        isLoggedIn = authService.isLoggedIn
    }

    fun login(username: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            error = null

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

            val success = userRepository.register(username, password)
            if (success) {
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
