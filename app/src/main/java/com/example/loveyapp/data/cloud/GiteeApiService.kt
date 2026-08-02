package com.example.loveyapp.data.cloud

import com.example.loveyapp.data.cloud.dto.GiteeContentDto
import com.example.loveyapp.data.cloud.dto.GiteeContentRequest
import com.example.loveyapp.data.cloud.dto.GiteeContentResponse
import com.example.loveyapp.data.cloud.dto.GiteeRepoDto
import com.google.gson.Gson
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Gitee API v5 客户端，基于 OkHttp + Gson 直接实现。
 *
 * **为何不使用 Retrofit？**
 * Retrofit 对 suspend 函数通过反射 `Continuation<T>` 参数的泛型签名解析返回类型。
 * R8 混淆会裁剪该方法参数的 `Signature` 属性，导致
 * `java.lang.Class cannot be cast to java.lang.reflect.ParameterizedType` 崩溃。
 * 改用 OkHttp 直接发请求 + Gson 手动解析，彻底消除反射类型解析依赖。
 *
 * 认证：`access_token` 查询参数（Gitee 官方推荐）。
 * 参考：https://gitee.com/api/v5/swagger
 */
@Singleton
class GiteeApiService @Inject constructor(
    private val client: OkHttpClient,
    private val gson: Gson
) {
    /** HTTP 非 2xx 时抛出，[code] 为状态码。 */
    class ApiException(val code: Int, val responseBody: String) : IOException("HTTP $code")

    private companion object {
        const val BASE_URL = "https://gitee.com/api/v5/"
        val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    /** 获取仓库信息（用于绑定校验，返回默认分支）。响应为空时返回 null。 */
    suspend fun getRepo(owner: String, repo: String, accessToken: String): GiteeRepoDto? {
        val url = BASE_URL.toHttpUrl().newBuilder()
            .addPathSegments("repos/$owner/$repo")
            .addQueryParameter("access_token", accessToken)
            .build()
        return parseJson(executeGet(url), GiteeRepoDto::class.java)
    }

    /**
     * 获取文件内容（用于下载备份或取得 sha）。
     * 文件不存在时抛 [ApiException]（code=404）。
     *
     * 注意：Gitee 对不存在的文件路径返回 200 + 空数组 `[]` 而非 404，
     * 此处检测数组响应并视为文件不存在。
     */
    suspend fun getContent(
        owner: String,
        repo: String,
        path: String,
        ref: String,
        accessToken: String
    ): GiteeContentDto? {
        val url = BASE_URL.toHttpUrl().newBuilder()
            .addPathSegments("repos/$owner/$repo/contents/$path")
            .addQueryParameter("ref", ref)
            .addQueryParameter("access_token", accessToken)
            .build()
        val body = executeGet(url)
        // Gitee 对不存在的文件路径返回空数组 [] 而非 404，视为文件不存在
        if (body.trimStart().startsWith("[")) {
            throw ApiException(404, body)
        }
        return parseJson(body, GiteeContentDto::class.java)
    }

    /** 新建文件（首次云备份）。 */
    suspend fun createContent(
        owner: String,
        repo: String,
        path: String,
        accessToken: String,
        body: GiteeContentRequest
    ): GiteeContentResponse? {
        val url = BASE_URL.toHttpUrl().newBuilder()
            .addPathSegments("repos/$owner/$repo/contents/$path")
            .addQueryParameter("access_token", accessToken)
            .build()
        return executeJson(url, "POST", body, GiteeContentResponse::class.java)
    }

    /** 更新已存在文件（需提供 sha）。 */
    suspend fun updateContent(
        owner: String,
        repo: String,
        path: String,
        accessToken: String,
        body: GiteeContentRequest
    ): GiteeContentResponse? {
        val url = BASE_URL.toHttpUrl().newBuilder()
            .addPathSegments("repos/$owner/$repo/contents/$path")
            .addQueryParameter("access_token", accessToken)
            .build()
        return executeJson(url, "PUT", body, GiteeContentResponse::class.java)
    }

    // ---- 内部辅助 ----

    private suspend fun executeGet(url: okhttp3.HttpUrl): String {
        val request = Request.Builder().url(url).get().build()
        return executeRequest(request)
    }

    private suspend fun <T> executeJson(
        url: okhttp3.HttpUrl,
        method: String,
        body: Any,
        resultClass: Class<T>
    ): T? {
        val json = gson.toJson(body)
        val requestBody: RequestBody = json.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder().url(url).method(method, requestBody).build()
        val responseStr = executeRequest(request)
        return parseJson(responseStr, resultClass)
    }

    /** 解析 JSON，失败时异常信息附带响应体片段便于诊断。 */
    private fun <T> parseJson(body: String, clazz: Class<T>): T? {
        if (body.isBlank()) return null
        return try {
            gson.fromJson(body, clazz)
        } catch (e: com.google.gson.JsonParseException) {
            val preview = if (body.length > 300) body.take(300) + "..." else body
            throw com.google.gson.JsonParseException("响应解析失败，原始内容: $preview", e)
        }
    }

    private suspend fun executeRequest(request: Request): String {
        val response = client.newCall(request).await()
        response.use {
            val bodyStr = it.body?.string() ?: ""
            android.util.Log.d("GiteeApi", "${request.method} ${it.code} bodyLen=${bodyStr.length}")
            if (!it.isSuccessful) {
                throw ApiException(it.code, bodyStr)
            }
            return bodyStr
        }
    }

    /** 将 OkHttp [Call] 转为 suspend 函数。 */
    private suspend fun Call.await(): Response = suspendCancellableCoroutine { cont ->
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                cont.resume(response)
            }
        })
        cont.invokeOnCancellation { runCatching { cancel() } }
    }
}
