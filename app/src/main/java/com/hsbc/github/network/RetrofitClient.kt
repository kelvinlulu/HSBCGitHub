package com.hsbc.github.network

import com.hsbc.github.GitHubApp
import com.hsbc.github.utils.SecureStorage
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.github.com/"

    val api: GitHubApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubApi::class.java)
    }

    val oauthApi = Retrofit.Builder()
        .baseUrl("https://github.com/")
        .addConverterFactory(GsonConverterFactory.create()) // ✅ Gson 转换器
        .build()
        .create(GitHubApi::class.java)

    // 新增：GitHub API 客户端（用于用户信息、仓库等）
    private val apiClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val token = SecureStorage(GitHubApp.applicationContext()).getAccessToken()

            if (token != null) {
                val request = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $token")
                    .build()
                chain.proceed(request)
            } else {
                chain.proceed(originalRequest)
            }
        }
        .build()

}