package com.hsbc.github.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit客户端配置与实例管理
 * 用于创建和管理Retrofit实例，提供API接口访问
 */
object RetrofitClient {

    /**
     * GitHub API主客户端
     * - 基础URL：https://api.github.com/（用于常规API请求）
     * - 使用Gson转换器解析JSON响应
     * - 采用懒加载方式初始化（首次使用时创建）
     */
    val api: GitHubApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubApi::class.java)
    }

    /**
     * OAuth认证专用客户端
     * - 基础URL：https://github.com/（用于OAuth认证流程）
     * - 与主客户端使用不同的BaseURL以适配认证接口
     * - 直接初始化实例（非懒加载）
     */
    val oauthApi: GitHubApi = Retrofit.Builder()
        .baseUrl("https://github.com/")
        .addConverterFactory(GsonConverterFactory.create()) // Gson转换器用于解析JSON响应
        .build()
        .create(GitHubApi::class.java)
}