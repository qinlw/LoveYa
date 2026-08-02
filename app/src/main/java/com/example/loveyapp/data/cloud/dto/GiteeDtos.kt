package com.example.loveyapp.data.cloud.dto

import com.google.gson.annotations.SerializedName

/**
 * Gitee API 响应 DTO。
 *
 * **注意**：Gson 通过 `Unsafe.allocateInstance()` 创建对象，不调用 Kotlin 构造函数，
 * 因此 Kotlin 默认值（如 `= "master"`）不会生效，缺失的 JSON 字段在运行时为 null。
 * 所有 String 字段标记为 nullable 以如实反映运行时状态。
 */

/** GET /repos/{owner}/{repo} 响应 */
data class GiteeRepoDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("default_branch") val defaultBranch: String? = null,
    @SerializedName("private") val isPrivate: Boolean = false
)

/** GET /repos/{owner}/{repo}/contents/{path} 响应 */
data class GiteeContentDto(
    @SerializedName("name") val name: String? = null,
    @SerializedName("path") val path: String? = null,
    @SerializedName("sha") val sha: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("content") val content: String? = null,
    @SerializedName("encoding") val encoding: String? = null
)

/** POST / PUT /repos/{owner}/{repo}/contents/{path} 请求体 */
data class GiteeContentRequest(
    @SerializedName("content") val content: String,
    @SerializedName("message") val message: String,
    @SerializedName("branch") val branch: String,
    @SerializedName("sha") val sha: String? = null
)

/** POST / PUT /repos/{owner}/{repo}/contents/{path} 响应 */
data class GiteeContentResponse(
    @SerializedName("content") val content: GiteeContentDto? = null,
    @SerializedName("commit") val commit: GiteeCommitDto? = null
)

data class GiteeCommitDto(
    @SerializedName("sha") val sha: String? = null,
    @SerializedName("message") val message: String? = null
)
