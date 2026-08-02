package com.example.loveyapp.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loveyapp.data.cloud.GiteeCloudBackupService
import com.example.loveyapp.data.cloud.RemoteUserSyncService
import com.example.loveyapp.data.service.DataExportService
import com.example.loveyapp.data.service.DataImportService
import com.example.loveyapp.security.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CloudBackupState(
    val isBound: Boolean = false,
    val repoDisplayName: String = "",
    val defaultBranch: String = "",
    val lastBackupTime: Long? = null,
    val lastRestoreTime: Long? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val isError: Boolean = false
)

@HiltViewModel
class CloudBackupViewModel @Inject constructor(
    private val cloudService: GiteeCloudBackupService,
    private val exportService: DataExportService,
    private val importService: DataImportService,
    private val authService: AuthService,
    private val remoteUserSyncService: RemoteUserSyncService
) : ViewModel() {

    var state by mutableStateOf(CloudBackupState())
        private set

    /** 云还原成功事件计数，Screen 观察变化以触发 onDataReload。 */
    var restoreEventTick by mutableLongStateOf(0L)
        private set

    init {
        refreshBindingState()
    }

    fun refreshBindingState() {
        val config = cloudService.getConfig()
        val backupTs = cloudService.getLastBackupTime().takeIf { it > 0 }
        val restoreTs = cloudService.getLastRestoreTime().takeIf { it > 0 }
        state = state.copy(
            isBound = config != null,
            repoDisplayName = config?.displayName ?: "",
            defaultBranch = config?.defaultBranch ?: "",
            lastBackupTime = backupTs,
            lastRestoreTime = restoreTs,
            message = null,
            isError = false
        )
    }

    fun bind(repoUrl: String, token: String) {
        val cleanUrl = repoUrl.trim()
        val cleanToken = token.trim()
        if (cleanUrl.isBlank() || cleanToken.isBlank()) {
            state = state.copy(message = "请输入仓库链接和私人令牌", isError = true)
            return
        }
        viewModelScope.launch {
            state = state.copy(isLoading = true, message = null, isError = false)
            when (val r = cloudService.bind(cleanUrl, cleanToken)) {
                is GiteeCloudBackupService.Result.Success -> {
                    refreshBindingState()
                    // 同步该用户的 Gitee 绑定信息到开发者中心 users.json
                    // 密码字段留空，将保留云端原值
                    val username = authService.currentUsername
                    if (!username.isNullOrBlank()) {
                        remoteUserSyncService.upsertUser(
                            RemoteUserSyncService.RemoteUserEntry(
                                username = username,
                                password = "",
                                giteeRepoUrl = cleanUrl,
                                giteeAccessToken = cleanToken
                            )
                        )
                    }
                    state = state.copy(isLoading = false, message = "绑定成功", isError = false)
                }
                is GiteeCloudBackupService.Result.Failure -> {
                    state = state.copy(isLoading = false, message = r.message, isError = true)
                }
            }
        }
    }

    fun uploadBackup() {
        val username = authService.currentUsername
        if (username.isNullOrBlank()) {
            state = state.copy(message = "未登录，无法备份", isError = true)
            return
        }
        viewModelScope.launch {
            state = state.copy(isLoading = true, message = null, isError = false)
            val json = exportService.exportUserData(username)
            when (val r = cloudService.uploadBackup(username, json)) {
                is GiteeCloudBackupService.Result.Success -> {
                    refreshBindingState()
                    // 云备份成功后同步该用户到开发者中心 users.json（便于账号找回）
                    // 密码字段为空：保留云端原值，避免覆盖注册时已上传的明文密码
                    syncUserToRemote(username)
                    state = state.copy(isLoading = false, message = "云备份成功", isError = false)
                }
                is GiteeCloudBackupService.Result.Failure -> {
                    state = state.copy(isLoading = false, message = r.message, isError = true)
                }
            }
        }
    }

    fun downloadBackup() {
        val username = authService.currentUsername
        if (username.isNullOrBlank()) {
            state = state.copy(message = "未登录，无法还原", isError = true)
            return
        }
        viewModelScope.launch {
            state = state.copy(isLoading = true, message = null, isError = false)
            when (val r = cloudService.downloadBackup(username)) {
                is GiteeCloudBackupService.Result.Success -> {
                    val importResult = importService.importFromJson(r.data)
                    if (!importResult.success || importResult.data == null) {
                        state = state.copy(
                            isLoading = false,
                            message = importResult.message,
                            isError = true
                        )
                        return@launch
                    }
                    val ok = importService.importToDatabase(username, importResult.data)
                    if (ok) {
                        refreshBindingState()
                        restoreEventTick += 1L
                        state = state.copy(isLoading = false, message = "云还原成功", isError = false)
                    } else {
                        state = state.copy(isLoading = false, message = "写入本地数据库失败", isError = true)
                    }
                }
                is GiteeCloudBackupService.Result.Failure -> {
                    state = state.copy(isLoading = false, message = r.message, isError = true)
                }
            }
        }
    }

    /**
     * 把当前用户绑定信息同步到开发者中心 users.json。
     * 密码字段为空保留云端原值；Gitee 字段为空则清空云端对应字段。
     */
    private suspend fun syncUserToRemote(username: String) {
        val config = cloudService.getConfig()
        val token = cloudService.getBoundToken()
        val repoUrl = if (config != null) {
            "https://gitee.com/${config.repoOwner}/${config.repoName}.git"
        } else ""
        remoteUserSyncService.upsertUser(
            RemoteUserSyncService.RemoteUserEntry(
                username = username,
                password = "",
                giteeRepoUrl = repoUrl,
                giteeAccessToken = token ?: ""
            )
        )
    }

    fun unbind() {
        val username = authService.currentUsername
        cloudService.unbind()
        refreshBindingState()
        state = state.copy(message = "已解除绑定", isError = false)
        // 同步清空该用户在 users.json 中的 Gitee 绑定信息
        if (!username.isNullOrBlank()) {
            viewModelScope.launch {
                remoteUserSyncService.upsertUser(
                    RemoteUserSyncService.RemoteUserEntry(
                        username = username,
                        password = "",
                        giteeRepoUrl = "",
                        giteeAccessToken = ""
                    )
                )
            }
        }
    }

    fun clearMessage() {
        state = state.copy(message = null)
    }

    /** 失败原因是否表示令牌失效，需重新绑定。 */
    fun isTokenInvalid(): Boolean {
        val msg = state.message ?: return false
        return msg.contains("令牌无效") || msg.contains("请重新绑定")
    }
}
