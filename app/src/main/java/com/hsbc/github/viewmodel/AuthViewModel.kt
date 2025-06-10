package com.hsbc.github.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hsbc.github.BuildConfig
import com.hsbc.github.GitHubApp
import com.hsbc.github.model.Repo
import com.hsbc.github.model.Repository
import com.hsbc.github.model.User
import com.hsbc.github.network.AccessTokenResponse
import com.hsbc.github.network.RetrofitClient
import com.hsbc.github.utils.SecureStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// AuthViewModel.kt
class AuthViewModel : ViewModel() {

    // 定义 UI 状态流
    private val _repos = MutableStateFlow<List<Repository>?>(null)
    val repos: StateFlow<List<Repository>?> = _repos

    private val _user = MutableStateFlow<User?>(null)
    val user : StateFlow<User?> = _user

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    suspend fun fetchAccessToken(authCode: String): AccessTokenResponse {
        return RetrofitClient.oauthApi.getAccessToken(
            clientId = "Iv23lizoSx3TJhhTZOom",
            clientSecret = "566f616ad112351c1d3ae2dfbe9f2df7f985013d",
            authCode = authCode
        )
    }

    private val secureStorage = SecureStorage(GitHubApp.applicationContext())

    // 新增：登录状态 LiveData
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> get() = _isLoggedIn

    init {
        // 初始化时检查登录状态
        _isLoggedIn.value = secureStorage.getAccessToken() != null
    }

    fun getAccessToken(): String {
        return secureStorage.getAccessToken() ?: ""
    }

    // AuthViewModel.kt
    suspend fun fetchUserInfo() {
        val token = secureStorage.getAccessToken() ?: return

        try {
            _user.value = RetrofitClient.api.getUser("Bearer $token") // 注意：此处需使用 GitHub API 的 baseUrl
        } catch (e: Exception) {
            Log.e("Auth", "Failed to fetch user info: ${e.message}")
            null
        }
    }

    suspend fun fetchAndSaveAccessToken(authCode: String): AccessTokenResponse {
        val response = fetchAccessToken(authCode)  // 获取Token
        secureStorage.saveAccessToken(response.access_token)  // 保存Token
        _isLoggedIn.value = true
        return response  // 可选：返回Token响应
    }

    suspend fun fetchUserRepositories() {
        val token = secureStorage.getAccessToken() ?: return
        try {
            _repos.value = RetrofitClient.api.getUserRepos("Bearer $token")
        } catch (e: Exception) {
            Log.e("Auth", "Failed to fetch repositories: ${e.message}")
            null
        }
    }

    // 新增：登出方法
    fun logout() {
        secureStorage.clearAccessToken()
        _isLoggedIn.value = false
    }

}