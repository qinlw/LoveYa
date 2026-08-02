package com.example.loveyapp.config

import com.example.loveyapp.BuildConfig

/**
 * 开发者中心仓库配置（私有，仅开发侧维护）。
 *
 * 集中管理存放所有用户账号数据的远程 Gitee 仓库信息。
 * 修改仓库位置 / 文件路径 / 令牌 只需调整本文件或 local.properties，无需改动业务代码。
 *
 * 安全策略：
 * - 仓库地址拆分存储，运行时拼接，避免完整 URL 出现在反编译产物中
 * - 访问令牌通过 [BuildConfig] 注入，来自不入库的 local.properties
 */
object DeveloperConfig {

    // ===== 用户中心数据仓库（存放 users.json） =====
    // 拆分存储，避免完整字符串出现在 dex 中
    private const val HOST = "gitee.com"
    private const val OWNER_PART = "qq2162036628"
    private const val REPO_PART = "love-ya-data"
    private const val USER_DATA_FILE = "users.json"
    private const val DEFAULT_BRANCH = "master"

    /** 仓库所有者（Gitee 用户名） */
    val repoOwner: String get() = OWNER_PART

    /** 仓库名 */
    val repoName: String get() = REPO_PART

    /** 默认分支 */
    val branch: String get() = DEFAULT_BRANCH

    /** users.json 在仓库中的相对路径 */
    val userDataFilePath: String get() = USER_DATA_FILE

    /** 完整仓库 HTTPS 地址（仅用于显示） */
    val repoUrl: String
        get() = "https://$HOST/${OWNER_PART}/${REPO_PART}.git"

    /**
     * 开发者中心访问令牌。
     *
     * 来源链路：local.properties -> DEV_GITEE_TOKEN -> BuildConfig.DEV_GITEE_TOKEN
     * local.properties 已被 .gitignore 排除，令牌不会进入版本控制。
     */
    val accessToken: String
        get() = BuildConfig.DEV_GITEE_TOKEN
}
