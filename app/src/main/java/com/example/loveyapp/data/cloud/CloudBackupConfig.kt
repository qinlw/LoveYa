package com.example.loveyapp.data.cloud

/**
 * 云备份绑定配置（内存数据类）。Token 仅通过 [CloudBackupConfigStore] 持久化，不出现在日志中。
 */
data class CloudBackupConfig(
    val repoOwner: String,
    val repoName: String,
    val defaultBranch: String = "master",
    val backupPath: String = DEFAULT_BACKUP_PATH,
    val boundAt: Long = System.currentTimeMillis()
) {
    val displayName: String
        get() = "$repoOwner/$repoName"

    companion object {
        /** 云端备份文件固定路径，单文件覆盖式。 */
        const val DEFAULT_BACKUP_PATH = "backups/loveya_backup.json"
    }
}
