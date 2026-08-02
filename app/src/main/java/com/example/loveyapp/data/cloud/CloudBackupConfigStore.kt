package com.example.loveyapp.data.cloud

import android.content.SharedPreferences
import com.example.loveyapp.security.KeyManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 云备份配置与令牌的加密持久化。
 *
 * 使用 [KeyManager] 派生的 [EncryptedSharedPreferences]（文件名 `cloud_backup`），
 * token 绝不明文落盘。
 */
@Singleton
class CloudBackupConfigStore @Inject constructor(
    private val keyManager: KeyManager
) {
    private val prefs: SharedPreferences by lazy {
        keyManager.getEncryptedSharedPreferences("cloud_backup")
    }

    fun saveConfig(config: CloudBackupConfig, accessToken: String) {
        prefs.edit()
            .putString(KEY_REPO_OWNER, config.repoOwner)
            .putString(KEY_REPO_NAME, config.repoName)
            .putString(KEY_DEFAULT_BRANCH, config.defaultBranch)
            .putString(KEY_BACKUP_PATH, config.backupPath)
            .putLong(KEY_BOUND_AT, config.boundAt)
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .apply()
    }

    fun getConfig(): CloudBackupConfig? {
        val owner = prefs.getString(KEY_REPO_OWNER, null) ?: return null
        val repo = prefs.getString(KEY_REPO_NAME, null) ?: return null
        return CloudBackupConfig(
            repoOwner = owner,
            repoName = repo,
            defaultBranch = prefs.getString(KEY_DEFAULT_BRANCH, "master") ?: "master",
            backupPath = prefs.getString(KEY_BACKUP_PATH, CloudBackupConfig.DEFAULT_BACKUP_PATH)
                ?: CloudBackupConfig.DEFAULT_BACKUP_PATH,
            boundAt = prefs.getLong(KEY_BOUND_AT, 0L)
        )
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun getLastBackupTime(): Long = prefs.getLong(KEY_LAST_BACKUP_TIME, 0L)

    fun setLastBackupTime(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_BACKUP_TIME, timestamp).apply()
    }

    fun getLastRestoreTime(): Long = prefs.getLong(KEY_LAST_RESTORE_TIME, 0L)

    fun setLastRestoreTime(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_RESTORE_TIME, timestamp).apply()
    }

    /** 解除绑定：清除全部相关键值，token 不可恢复。 */
    fun clear() {
        prefs.edit().clear().apply()
    }

    val isBound: Boolean
        get() = getConfig() != null && getAccessToken() != null

    companion object {
        private const val KEY_REPO_OWNER = "repo_owner"
        private const val KEY_REPO_NAME = "repo_name"
        private const val KEY_DEFAULT_BRANCH = "default_branch"
        private const val KEY_BACKUP_PATH = "backup_path"
        private const val KEY_BOUND_AT = "bound_at"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_LAST_BACKUP_TIME = "last_backup_time"
        private const val KEY_LAST_RESTORE_TIME = "last_restore_time"
    }
}
