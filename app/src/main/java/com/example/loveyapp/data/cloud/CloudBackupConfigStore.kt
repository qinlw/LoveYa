package com.example.loveyapp.data.cloud

import android.content.SharedPreferences
import com.example.loveyapp.security.AuthService
import com.example.loveyapp.security.KeyManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 云备份配置与令牌的加密持久化。
 *
 * 使用 [KeyManager] 派生的 [EncryptedSharedPreferences]，
 * 文件名按当前登录用户区分（`cloud_backup_{username}`），
 * token 绝不明文落盘，且不同账号绑定信息互不影响。
 */
@Singleton
class CloudBackupConfigStore @Inject constructor(
    private val keyManager: KeyManager,
    private val authService: AuthService
) {
    /** 缓存已创建的 SharedPreferences 实例，避免重复解密开销。 */
    private val prefsCache = mutableMapOf<String, SharedPreferences>()

    private fun prefs(): SharedPreferences {
        val username = authService.currentUsername
            ?: throw IllegalStateException("未登录，无法访问云备份配置")
        return prefsCache.getOrPut(username) {
            keyManager.getEncryptedSharedPreferences("cloud_backup_$username")
        }
    }

    fun saveConfig(config: CloudBackupConfig, accessToken: String) {
        prefs().edit()
            .putString(KEY_REPO_OWNER, config.repoOwner)
            .putString(KEY_REPO_NAME, config.repoName)
            .putString(KEY_DEFAULT_BRANCH, config.defaultBranch)
            .putString(KEY_BACKUP_PATH, config.backupPath)
            .putLong(KEY_BOUND_AT, config.boundAt)
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .apply()
    }

    fun getConfig(): CloudBackupConfig? {
        val p = try { prefs() } catch (e: Exception) { return null }
        val owner = p.getString(KEY_REPO_OWNER, null) ?: return null
        val repo = p.getString(KEY_REPO_NAME, null) ?: return null
        return CloudBackupConfig(
            repoOwner = owner,
            repoName = repo,
            defaultBranch = p.getString(KEY_DEFAULT_BRANCH, "master") ?: "master",
            backupPath = p.getString(KEY_BACKUP_PATH, CloudBackupConfig.DEFAULT_BACKUP_PATH)
                ?: CloudBackupConfig.DEFAULT_BACKUP_PATH,
            boundAt = p.getLong(KEY_BOUND_AT, 0L)
        )
    }

    fun getAccessToken(): String? = try { prefs().getString(KEY_ACCESS_TOKEN, null) } catch (e: Exception) { null }

    fun getLastBackupTime(): Long = try { prefs().getLong(KEY_LAST_BACKUP_TIME, 0L) } catch (e: Exception) { 0L }

    fun setLastBackupTime(timestamp: Long) {
        try { prefs().edit().putLong(KEY_LAST_BACKUP_TIME, timestamp).apply() } catch (_: Exception) {}
    }

    fun getLastRestoreTime(): Long = try { prefs().getLong(KEY_LAST_RESTORE_TIME, 0L) } catch (e: Exception) { 0L }

    fun setLastRestoreTime(timestamp: Long) {
        try { prefs().edit().putLong(KEY_LAST_RESTORE_TIME, timestamp).apply() } catch (_: Exception) {}
    }

    /** 解除绑定：清除当前用户全部相关键值，token 不可恢复。 */
    fun clear() {
        try { prefs().edit().clear().apply() } catch (_: Exception) {}
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
