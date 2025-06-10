package com.hsbc.github.network

import com.hsbc.github.model.Repo
import com.hsbc.github.model.Repository
import com.hsbc.github.model.User
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApi {
    @GET("repositories")
    suspend fun getRepositories(): List<Repo>

    @GET("repos/{owner}/{name}")
    suspend fun getRepoDetails(
        @Path("owner") owner: String,
        @Path("name") name: String,
    ): Repo

    // 新增搜索接口，支持语言和排序参数
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,       // 搜索关键词（如 "language:kotlin"）
        @Query("sort") sort: String = "stars", // 排序方式（stars/updated）
        @Query("order") order: String = "desc"  // 升降序
    ): RepoSearchResponse

    // 登录获取 AccessToken
    @POST("login/oauth/access_token")
    @FormUrlEncoded
    @Headers("Accept: application/json")
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") authCode: String
    ): AccessTokenResponse

    // 新增：获取当前用户仓库列表（需要认证）
    @GET("user/repos")
    suspend fun getMyRepositories(
        @Header("Authorization") token: String,
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 30
    ): List<Repo>

    // 新增：获取用户信息
    @GET("user")
    suspend fun getUser(@Header("Authorization") token: String): User

    @GET("user/repos")
    suspend fun getUserRepos(
        @Header("Authorization") token: String,
        @Query("per_page") perPage: Int = 100,
        @Query("sort") sort: String = "updated"
    ): List<Repository>
}

data class RepoSearchResponse(
    val items: List<Repo>
)

// 数据模型
data class AccessTokenResponse(
    val access_token: String,
    val token_type: String,
    val scope: String
)

