package com.example.loveyapp.data.cloud

import javax.inject.Inject
import javax.inject.Singleton

/**
 * 解析 Gitee 仓库链接，统一得到 [owner] / [repo]。
 *
 * 支持格式：
 * - `https://gitee.com/{owner}/{repo}`
 * - `https://gitee.com/{owner}/{repo}.git`
 * - `https://gitee.com/{owner}/{repo}/`（末尾斜杠）
 * - `git@gitee.com:{owner}/{repo}.git`
 */
@Singleton
class GiteeRepositoryParser @Inject constructor() {

    data class RepoRef(val owner: String, val repo: String)

    /**
     * 解析仓库链接。失败返回 null。
     */
    fun parse(url: String): RepoRef? {
        val trimmed = url.trim()
        if (trimmed.isEmpty()) return null

        return runCatching {
            when {
                trimmed.startsWith("git@gitee.com:") -> parseSsh(trimmed)
                trimmed.contains("gitee.com/") -> parseHttps(trimmed)
                else -> null
            }
        }.getOrNull()
    }

    private fun parseHttps(url: String): RepoRef? {
        // 去掉 query / fragment
        val noQuery = url.substringBefore('#').substringBefore('?')
        // 去掉协议
        val afterHost = noQuery.substringAfter("gitee.com/")
        val parts = afterHost.trimEnd('/').split('/')
        if (parts.size < 2) return null
        val owner = parts[0].trim()
        var repo = parts[1].trim()
        if (repo.endsWith(".git")) repo = repo.removeSuffix(".git")
        if (owner.isEmpty() || repo.isEmpty()) return null
        return RepoRef(owner, repo)
    }

    private fun parseSsh(url: String): RepoRef? {
        // git@gitee.com:{owner}/{repo}.git
        val afterColon = url.substringAfter("gitee.com:")
        val parts = afterColon.trimEnd('/').split('/')
        if (parts.size < 2) return null
        val owner = parts[0].trim()
        var repo = parts[1].trim()
        if (repo.endsWith(".git")) repo = repo.removeSuffix(".git")
        if (owner.isEmpty() || repo.isEmpty()) return null
        return RepoRef(owner, repo)
    }
}
