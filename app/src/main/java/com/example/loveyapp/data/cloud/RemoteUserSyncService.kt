package com.example.loveyapp.data.cloud

import android.util.Log
import com.example.loveyapp.config.DeveloperConfig
import com.example.loveyapp.data.cloud.dto.GiteeContentRequest
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 远端用户数据同步服务。
 *
 * 把"用户名 / 密码 / Gitee 仓库链接 / Gitee 令牌"汇总上传到开发者中心仓库
 * [DeveloperConfig] 指定的 users.json 文件，便于用户多设备异地数据找回。
 *
 * 触发时机：
 * - 用户注册
 * - 绑定 Gitee
 * - 用户名 / 密码 / Gitee 绑定变更
 *
 * 设计要点：
 * - 先 GET users.json 取 sha + 当前内容，存在则 PUT 更新，不存在（404）则 POST 新建
 * - 同一用户名存在则合并更新（空字段保留原值），不存在则追加
 * - 上传失败仅记录日志，不阻断业务流程
 * - 全程不打印 token / 密码
 */
@Singleton
class RemoteUserSyncService @Inject constructor(
    private val apiService: GiteeApiService,
    private val gson: Gson
) {

    /** 单条用户记录。@SerializedName 固定 JSON key，防止 R8 混淆字段名。 */
    data class RemoteUserEntry(
        @SerializedName("username") val username: String,
        @SerializedName("password") val password: String,
        @SerializedName("gitee_repo_url") val giteeRepoUrl: String = "",
        @SerializedName("gitee_access_token") val giteeAccessToken: String = "",
        @SerializedName("updated_at") val updatedAt: Long = System.currentTimeMillis()
    )

    private val tag = "RemoteUserSync"

    /**
     * 上传/更新单个用户条目。
     *
     * 流程：拉取现有 users.json -> 合并当前用户 -> 写回云端。
     * 任一环节失败均不影响调用方主流程，仅记录日志。
     *
     * @param entry 待写入的条目。空字符串字段（如 [RemoteUserEntry.password]）
     *              在已存在同名用户时会保留云端原值，便于"绑定 Gitee"场景下不重置密码。
     */
    suspend fun upsertUser(entry: RemoteUserEntry) {
        val token = DeveloperConfig.accessToken
        if (token.isEmpty()) {
            Log.w(tag, "DEV_GITEE_TOKEN 未配置，跳过同步")
            return
        }
        try {
            val existing = fetchExistingUsers(token)
            val sha = existing.first
            val list = existing.second.toMutableList()

            val idx = list.indexOfFirst { it.username == entry.username }
            val merged = if (idx >= 0) {
                val old = list[idx]
                entry.copy(
                    password = entry.password.ifBlank { old.password },
                    giteeRepoUrl = entry.giteeRepoUrl.ifBlank { old.giteeRepoUrl },
                    giteeAccessToken = entry.giteeAccessToken.ifBlank { old.giteeAccessToken }
                )
            } else {
                entry
            }

            if (idx >= 0) list[idx] = merged else list.add(merged)

            uploadUsers(list, sha, token)
            Log.i(tag, "Sync success: ${entry.username} (total=${list.size})")
        } catch (e: Throwable) {
            Log.w(tag, "Sync failed for ${entry.username}: ${e.javaClass.simpleName}: ${e.message}")
        }
    }

    /** 拉取并解析 users.json，文件不存在（404）返回空列表 + sha=null。 */
    private suspend fun fetchExistingUsers(token: String): Pair<String?, List<RemoteUserEntry>> {
        return try {
            val dto = apiService.getContent(
                owner = DeveloperConfig.repoOwner,
                repo = DeveloperConfig.repoName,
                path = DeveloperConfig.userDataFilePath,
                ref = DeveloperConfig.branch,
                accessToken = token
            ) ?: return null to emptyList()
            val sha = dto.sha
            val raw = decodeContent(dto.content, dto.encoding)
            Log.d(tag, "fetchExistingUsers: sha=$sha rawLen=${raw.length} raw=$raw")
            val list: List<RemoteUserEntry> = if (raw.isBlank() || raw == "[]") emptyList()
            else {
                val arr = gson.fromJson(raw, Array<RemoteUserEntry>::class.java)
                arr?.toList() ?: emptyList()
            }
            sha to list
        } catch (e: GiteeApiService.ApiException) {
            if (e.code == 404) null to emptyList() else throw e
        }
    }

    /** 将列表序列化并上传，存在 sha 走 PUT，否则 POST 新建。 */
    private suspend fun uploadUsers(list: List<RemoteUserEntry>, sha: String?, token: String) {
        val json = gson.toJson(list)
        Log.d(tag, "uploadUsers: sha=$sha jsonLen=${json.length} json=$json")
        val encoded = Base64.getEncoder().encodeToString(json.toByteArray(Charsets.UTF_8))
        val message = "sync user data ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}"
        val req = GiteeContentRequest(
            content = encoded,
            message = message,
            branch = DeveloperConfig.branch,
            sha = sha
        )
        if (sha != null) {
            apiService.updateContent(
                owner = DeveloperConfig.repoOwner,
                repo = DeveloperConfig.repoName,
                path = DeveloperConfig.userDataFilePath,
                accessToken = token,
                body = req
            )
        } else {
            apiService.createContent(
                owner = DeveloperConfig.repoOwner,
                repo = DeveloperConfig.repoName,
                path = DeveloperConfig.userDataFilePath,
                accessToken = token,
                body = req
            )
        }
    }

    private fun decodeContent(content: String?, encoding: String?): String {
        if (content.isNullOrEmpty()) return ""
        if (encoding == "base64" || encoding == null) {
            val cleaned = content.replace("\n", "").replace("\r", "")
            return String(Base64.getMimeDecoder().decode(cleaned), Charsets.UTF_8)
        }
        return content
    }
}
