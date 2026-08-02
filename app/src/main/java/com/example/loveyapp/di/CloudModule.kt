package com.example.loveyapp.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 云备份相关依赖（OkHttp）。
 *
 * GiteeApiService 使用 @Inject 构造函数注入，无需在此手动 provide。
 * 不再使用 Retrofit（R8 混淆会裁剪 suspend 函数 Continuation 泛型签名导致崩溃）。
 */
@Module
@InstallIn(SingletonComponent::class)
object CloudModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            // access_token 通过查询参数传递，URL 中会包含令牌，
            // 因此关闭 HTTP 日志，错误信息由 GiteeCloudBackupService 的 Log 记录
            level = HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }
}
