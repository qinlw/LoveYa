package com.example.loveyapp.data.cloud

import android.util.Log
import com.example.loveyapp.data.cloud.dto.GiteeContentRequest
import com.google.gson.JsonParseException
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 云备份编排服务：绑定仓库 / 上传备份 / 下载备份。
 *
 * 设计要点：
 * - 全程不打印 token，仅记录错误码与状态。
 * - API 基于 OkHttp 直接实现（无 Retrofit），HTTP 错误抛 [GiteeApiService.ApiException]。
 * - 上传逻辑：先 GET 取 sha，存在则 PUT 更新，不存在（404）则 POST 新建。
 * - Base64 编码使用 NO_WRAP（[Base64.getEncoder]），解码使用 MIME 解码器以兼容换行。
 * - 异常分类：ApiException（HTTP 错误码）/ IOException（网络）/ JsonParseException（解析）/ 其他。
 *   注意 [GiteeApiService.ApiException] 继承 [IOException]，必须在其之前捕获。
 */
@Singleton
class GiteeCloudBackupService @Inject constructor(
    private val apiService: GiteeApiService,
    private val parser: GiteeRepositoryParser,
    private val configStore: CloudBackupConfigStore
) {

    /** 统一结果封装。 */
    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Failure(val error: CloudBackupError, val message: String) : Result<Nothing>()
    }

    enum class CloudBackupError {
        PARSE_ERROR,
        NOT_BOUND,
        NETWORK_ERROR,
        UNAUTHORIZED,
        NOT_FOUND,
        FORBIDDEN,
        RATE_LIMITED,
        EMPTY_BACKUP,
        UNKNOWN
    }

    private val tag = "GiteeCloudBackup"

    /**
     * 绑定仓库：解析链接 → 校验令牌与仓库可达性 → 持久化配置。
     */
    suspend fun bind(repoUrl: String, accessToken: String): Result<CloudBackupConfig> {
        val ref = parser.parse(repoUrl)
            ?: return Result.Failure(CloudBackupError.PARSE_ERROR, "请输入有效的 Gitee 仓库 HTTPS 链接")

        return try {
            val dto = apiService.getRepo(
                owner = ref.owner,
                repo = ref.repo,
                accessToken = accessToken
            ) ?: return Result.Failure(CloudBackupError.PARSE_ERROR, "服务器返回空响应，请稍后重试")
            val branch = dto.defaultBranch?.takeIf { it.isNotEmpty() } ?: "master"
            val config = CloudBackupConfig(
                repoOwner = ref.owner,
                repoName = ref.repo,
                defaultBranch = branch
            )
            configStore.saveConfig(config, accessToken)
            Log.i(tag, "Bind success: ${config.displayName} branch=$branch")
            Result.Success(config)
        } catch (e: GiteeApiService.ApiException) {
            Log.w(tag, "bind http error: ${e.code}")
            when (e.code) {
                401 -> Result.Failure(CloudBackupError.UNAUTHORIZED, "令牌无效，请重新生成")
                403 -> Result.Failure(CloudBackupError.FORBIDDEN, "无法访问仓库，请检查令牌权限")
                404 -> Result.Failure(CloudBackupError.NOT_FOUND, "仓库不存在或令牌无权限访问")
                429 -> Result.Failure(CloudBackupError.RATE_LIMITED, "操作过于频繁，请稍后再试")
                else -> Result.Failure(CloudBackupError.UNKNOWN, "绑定失败（HTTP ${e.code}）")
            }
        } catch (e: IOException) {
            Log.w(tag, "bind network error: ${e.javaClass.simpleName}: ${e.message}")
            Result.Failure(CloudBackupError.NETWORK_ERROR, "请检查网络连接")
        } catch (e: JsonParseException) {
            Log.w(tag, "bind parse error: ${e.message}")
            Result.Failure(CloudBackupError.PARSE_ERROR, "服务器响应解析失败：${e.message ?: "未知错误"}")
        } catch (e: Exception) {
            Log.w(tag, "bind unexpected error: ${e.javaClass.simpleName}: ${e.message}")
            Result.Failure(CloudBackupError.UNKNOWN, "请求失败：${e.message ?: "未知错误"}")
        }
    }

    /**
     * 上传备份：将 JSON 内容写入云端按用户区分的路径 `backups/{username}/loveya_backup.json`。
     * 成功后更新 last_backup_time。
     */
    suspend fun uploadBackup(username: String, jsonContent: String): Result<Unit> {
        if (jsonContent.isEmpty()) {
            return Result.Failure(CloudBackupError.EMPTY_BACKUP, "导出数据为空，无法备份")
        }
        val config = configStore.getConfig()
            ?: return Result.Failure(CloudBackupError.NOT_BOUND, "尚未绑定仓库")
        val token = configStore.getAccessToken()
            ?: return Result.Failure(CloudBackupError.NOT_BOUND, "尚未绑定仓库")

        val backupPath = backupPathFor(username)
        val encoded = Base64.getEncoder().encodeToString(jsonContent.toByteArray(Charsets.UTF_8))
        val message = "LoveYa cloud backup ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"

        return try {
            val existingSha = fetchSha(config, backupPath, token)
            if (existingSha != null) {
                val req = GiteeContentRequest(
                    content = encoded,
                    message = message,
                    branch = config.defaultBranch,
                    sha = existingSha
                )
                apiService.updateContent(
                    owner = config.repoOwner,
                    repo = config.repoName,
                    path = backupPath,
                    accessToken = token,
                    body = req
                )
                configStore.setLastBackupTime(System.currentTimeMillis())
                Log.i(tag, "Upload(update) success: $backupPath")
                Result.Success(Unit)
            } else {
                val req = GiteeContentRequest(
                    content = encoded,
                    message = message,
                    branch = config.defaultBranch
                )
                apiService.createContent(
                    owner = config.repoOwner,
                    repo = config.repoName,
                    path = backupPath,
                    accessToken = token,
                    body = req
                )
                configStore.setLastBackupTime(System.currentTimeMillis())
                Log.i(tag, "Upload(create) success: $backupPath")
                Result.Success(Unit)
            }
        } catch (e: GiteeApiService.ApiException) {
            Log.w(tag, "upload http error: ${e.code}")
            when (e.code) {
                401 -> Result.Failure(CloudBackupError.UNAUTHORIZED, "令牌无效，请重新绑定")
                403 -> Result.Failure(CloudBackupError.FORBIDDEN, "无法访问仓库，请检查令牌权限")
                404 -> Result.Failure(CloudBackupError.NOT_FOUND, "仓库或文件路径不存在")
                429 -> Result.Failure(CloudBackupError.RATE_LIMITED, "操作过于频繁，请稍后再试")
                else -> Result.Failure(CloudBackupError.UNKNOWN, "云备份失败（HTTP ${e.code}）")
            }
        } catch (e: IOException) {
            Log.w(tag, "upload network error: ${e.javaClass.simpleName}: ${e.message}")
            Result.Failure(CloudBackupError.NETWORK_ERROR, "请检查网络连接")
        } catch (e: JsonParseException) {
            Log.w(tag, "upload parse error: ${e.message}")
            Result.Failure(CloudBackupError.PARSE_ERROR, "服务器响应解析失败：${e.message ?: "未知错误"}")
        } catch (e: Exception) {
            Log.w(tag, "upload unexpected error: ${e.javaClass.simpleName}: ${e.message}")
            Result.Failure(CloudBackupError.UNKNOWN, "请求失败：${e.message ?: "未知错误"}")
        }
    }

    /**
     * 下载备份：从云端按用户区分的路径 `backups/{username}/loveya_backup.json` 拉取 JSON 字符串。
     */
    suspend fun downloadBackup(username: String): Result<String> {
        val config = configStore.getConfig()
            ?: return Result.Failure(CloudBackupError.NOT_BOUND, "尚未绑定仓库")
        val token = configStore.getAccessToken()
            ?: return Result.Failure(CloudBackupError.NOT_BOUND, "尚未绑定仓库")

        val backupPath = backupPathFor(username)
        return try {
            val dto = apiService.getContent(
                owner = config.repoOwner,
                repo = config.repoName,
                path = backupPath,
                ref = config.defaultBranch,
                accessToken = token
            ) ?: return Result.Failure(CloudBackupError.PARSE_ERROR, "服务器返回空响应，请稍后重试")
            val content = dto.content
            if (content.isNullOrEmpty()) {
                Result.Failure(CloudBackupError.EMPTY_BACKUP, "云端备份内容为空")
            } else {
                val json = decodeContent(content, dto.encoding)
                configStore.setLastRestoreTime(System.currentTimeMillis())
                Log.i(tag, "Download success, size=${json.length}")
                Result.Success(json)
            }
        } catch (e: GiteeApiService.ApiException) {
            Log.w(tag, "download http error: ${e.code}")
            when (e.code) {
                401 -> Result.Failure(CloudBackupError.UNAUTHORIZED, "令牌无效，请重新绑定")
                403 -> Result.Failure(CloudBackupError.FORBIDDEN, "无法访问仓库，请检查令牌权限")
                404 -> Result.Failure(CloudBackupError.NOT_FOUND, "云端暂无备份，请先执行云备份")
                429 -> Result.Failure(CloudBackupError.RATE_LIMITED, "操作过于频繁，请稍后再试")
                else -> Result.Failure(CloudBackupError.UNKNOWN, "云还原失败（HTTP ${e.code}）")
            }
        } catch (e: IOException) {
            Log.w(tag, "download network error: ${e.javaClass.simpleName}: ${e.message}")
            Result.Failure(CloudBackupError.NETWORK_ERROR, "请检查网络连接")
        } catch (e: JsonParseException) {
            Log.w(tag, "download parse error: ${e.message}")
            Result.Failure(CloudBackupError.PARSE_ERROR, "服务器响应解析失败：${e.message ?: "未知错误"}")
        } catch (e: Exception) {
            Log.w(tag, "download unexpected error: ${e.javaClass.simpleName}: ${e.message}")
            Result.Failure(CloudBackupError.UNKNOWN, "请求失败：${e.message ?: "未知错误"}")
        }
    }

    /** 解除绑定，清除本地配置与令牌。 */
    fun unbind() {
        configStore.clear()
        Log.i(tag, "Unbind: config cleared")
    }

    /** 获取已存配置（用于 UI 显示绑定状态）。 */
    fun getConfig(): CloudBackupConfig? = configStore.getConfig()

    fun getLastBackupTime(): Long = configStore.getLastBackupTime()

    fun getLastRestoreTime(): Long = configStore.getLastRestoreTime()

    val isBound: Boolean
        get() = configStore.isBound

    // ---- 内部辅助 ----

    /** 取云端文件 sha，文件不存在（404）返回 null，其他错误向上抛。 */
    private suspend fun fetchSha(config: CloudBackupConfig, path: String, token: String): String? {
        return try {
            apiService.getContent(
                owner = config.repoOwner,
                repo = config.repoName,
                path = path,
                ref = config.defaultBranch,
                accessToken = token
            )?.sha
        } catch (e: GiteeApiService.ApiException) {
            if (e.code == 404) null else throw e
        }
    }

    /** 按用户名生成云端备份路径。 */
    private fun backupPathFor(username: String): String {
        return "backups/$username/loveya_backup.json"
    }

    /** 暴露已绑定仓库的访问令牌，供 ViewModel 同步 users.json 使用。 */
    fun getBoundToken(): String? = configStore.getAccessToken()

    private fun decodeContent(content: String, encoding: String?): String {
        if (encoding == "base64" || encoding == null) {
            // Gitee 返回的 base64 可能含换行，用 MIME 解码器兼容
            val cleaned = content.replace("\n", "").replace("\r", "")
            return String(Base64.getMimeDecoder().decode(cleaned), Charsets.UTF_8)
        }
        return content
    }
}
