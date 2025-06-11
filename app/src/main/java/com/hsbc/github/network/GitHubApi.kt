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

/**
 * GitHub API 接口定义
 * 包含与GitHub交互的所有API请求方法
 */
interface GitHubApi {

    /**
     * 获取公共仓库列表
     * @return 仓库列表数据
     */
    @GET("repositories")
    suspend fun getRepositories(): List<Repo>

    /**
     * 获取指定仓库的详细信息
     * @param owner 仓库所有者登录名
     * @param name 仓库名称
     * @return 仓库详细数据
     */
    @GET("repos/{owner}/{name}")
    suspend fun getRepoDetails(
        @Path("owner") owner: String,
        @Path("name") name: String
    ): Repo

    /**
     * 搜索仓库
     * @param query 搜索关键词（格式：key:value，如 language:kotlin）
     * @param sort 排序字段（stars:按星标数，updated:按更新时间）
     * @param order 排序顺序（asc:升序，desc:降序）
     * @return 搜索结果包含的仓库列表
     */
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc"
    ): RepoSearchResponse

    /**
     * 通过OAuth授权码获取访问令牌
     * @param clientId GitHub应用客户端ID
     * @param clientSecret GitHub应用客户端密钥
     * @param authCode 授权回调返回的临时授权码
     * @return 包含访问令牌的响应
     */
    @POST("login/oauth/access_token")
    @FormUrlEncoded
    @Headers("Accept: application/json")
    suspend fun getAccessToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") authCode: String
    ): AccessTokenResponse

    /**
     * 获取当前登录用户的信息
     * @param token 访问令牌（格式：Bearer {token}）
     * @return 用户基本信息
     */
    @GET("user")
    suspend fun getUser(@Header("Authorization") token: String): User

    /**
     * 获取当前登录用户的仓库列表
     * @param token 访问令牌（格式：Bearer {token}）
     * @param perPage 每页返回数量（最大100）
     * @param sort 排序方式（updated:按更新时间，created:按创建时间）
     * @return 用户仓库列表
     */
    @GET("user/repos")
    suspend fun getUserRepos(
        @Header("Authorization") token: String,
        @Query("per_page") perPage: Int = 100,
        @Query("sort") sort: String = "updated"
    ): List<Repository>
}

/**
 * 仓库搜索响应数据模型
 * 包含搜索结果中的仓库列表
 */
data class RepoSearchResponse(
    /** 搜索结果中的仓库列表 */
    val items: List<Repo>
)

/**
 * 访问令牌响应数据模型
 * 包含OAuth授权返回的访问令牌信息
 */
data class AccessTokenResponse(
    /** 访问令牌（用于API认证） */
    val access_token: String,
    /** 令牌类型（通常为"bearer"） */
    val token_type: String,
    /** 令牌授权范围 */
    val scope: String
)