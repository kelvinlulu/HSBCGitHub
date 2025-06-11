package com.hsbc.github.viewmodel

import androidx.lifecycle.ViewModel
import com.hsbc.github.GitHubApp
import com.hsbc.github.model.Repository
import com.hsbc.github.model.User
import com.hsbc.github.network.AccessTokenResponse
import com.hsbc.github.network.RetrofitClient
import com.hsbc.github.utils.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * 认证视图模型
 * 管理用户登录状态、访问令牌及用户数据获取逻辑
 */
class AuthViewModel : ViewModel() {

    private val secureStorage = SecureStorage(GitHubApp.applicationContext())

    /** 用户仓库列表状态流 */
    private val _repos = MutableStateFlow<List<Repository>?>(null)
    val repos: StateFlow<List<Repository>?> = _repos

    /** 加载状态流（true表示正在进行网络请求） */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** 错误信息状态流（网络请求失败时返回错误信息） */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    /** 登录状态流（true表示用户已登录） */
    private val _isLoggedIn = MutableStateFlow<Boolean>(false)
    val isLoggedIn: StateFlow<Boolean> get() = _isLoggedIn

    init {
        // 初始化时检查登录状态（基于本地存储的访问令牌）
        _isLoggedIn.value = secureStorage.getAccessToken() != null
    }

    /**
     * 获取并保存访问令牌
     * @param authCode 临时授权码（来自OAuth回调）
     * @return 操作结果（true表示成功，false表示失败）
     */
    suspend fun fetchAndSaveAccessToken(authCode: String): Boolean {
        _isLoading.value = true
        _errorMessage.value = null

        return try {
            val response = fetchAccessToken(authCode)
            secureStorage.saveAccessToken(response.access_token)
            _isLoggedIn.value = true
            true
        } catch (e: Exception) {
            _errorMessage.value = "获取访问令牌失败: ${e.message}"
            false
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * 获取用户信息并保存
     * 需已获取访问令牌
     */
    suspend fun fetchUserInfo() {
        val token = secureStorage.getAccessToken() ?: return

        _isLoading.value = true
        _errorMessage.value = null

        try {
            val user = RetrofitClient.api.getUser("Bearer $token")
            _user.value = user
            secureStorage.saveUserName(user.name ?: "")
            secureStorage.saveUserLogin(user.login)
        } catch (e: Exception) {
            _errorMessage.value = "获取用户信息失败: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * 获取用户仓库列表
     * 需已获取访问令牌
     */
    suspend fun fetchUserRepositories() {
        val token = secureStorage.getAccessToken() ?: return

        _isLoading.value = true
        _errorMessage.value = null

        try {
            val repos = RetrofitClient.api.getUserRepos("Bearer $token")
            _repos.value = repos
        } catch (e: Exception) {
            _errorMessage.value = "获取仓库列表失败: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    /**
     * 退出登录
     * 清除所有用户数据并更新登录状态
     */
    fun logout() {
        secureStorage.clearAllUserData()
        _isLoggedIn.value = false
        _repos.value = null
    }

    /**
     * 内部方法：获取访问令牌（通过OAuth接口）
     * @param authCode 临时授权码
     * @return 访问令牌响应数据
     */
    private suspend fun fetchAccessToken(authCode: String): AccessTokenResponse {
        return RetrofitClient.oauthApi.getAccessToken(
            clientId = "Iv23lizoSx3TJhhTZOom",
            clientSecret = "566f616ad112351c1d3ae2dfbe9f2df7f985013d",
            authCode = authCode
        )
    }

    /**
     * 获取访问令牌（供外部使用）
     * @return 访问令牌（若无则返回空字符串）
     */
    fun getAccessToken(): String {
        return secureStorage.getAccessToken() ?: ""
    }

    /**
     * 获取用户登录名
     * @return 用户登录名（若无则返回空字符串）
     */
    fun getUserLogin(): String {
        return secureStorage.getUserLogin() ?: ""
    }

    /**
     * 获取用户真实姓名
     * @return 用户真实姓名（若无则返回空字符串）
     */
    fun getUserName(): String {
        return secureStorage.getUserName() ?: ""
    }
}