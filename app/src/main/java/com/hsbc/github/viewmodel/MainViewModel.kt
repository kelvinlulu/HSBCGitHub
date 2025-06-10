package com.hsbc.github.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsbc.github.model.Repo
import com.hsbc.github.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    // 定义 UI 状态流
    private val _repos = MutableStateFlow<List<Repo>?>(null)
    val repos: StateFlow<List<Repo>?> = _repos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        fetchRepositories()
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> get() = _searchQuery

    // 触发搜索的方法
    suspend fun search(language: String, sort: String = "stars") {
        val query = "language:$language"
        _searchQuery.value = query
        fetchRepositories(query, sort)
    }

    private suspend fun fetchRepositories(query: String, sort: String) {
        try {
            // 处理 GitHub API 搜索请求
            _isLoading.value = true
            val result = RetrofitClient.api.searchRepositories(query, sort)
            _repos.value = result.items
            _errorMessage.value = null

        } catch (e: Exception) {
            _errorMessage.value = "搜索仓库失败: ${e.message}"
            e.printStackTrace()
        } finally {
            _isLoading.value = false
        }
    }

    // 获取仓库数据
    fun fetchRepositories() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = RetrofitClient.api.getRepositories()
                _repos.value = result
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "获取仓库失败: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

}