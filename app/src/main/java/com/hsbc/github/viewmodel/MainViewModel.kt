package com.hsbc.github.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsbc.github.model.Repo
import com.hsbc.github.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 主页面视图模型
 * 负责获取和管理GitHub仓库数据，支持搜索和排序功能
 */
class MainViewModel : ViewModel() {

    /** 仓库列表状态流（包含仓库数据） */
    private val _repos = MutableStateFlow<List<Repo>?>(null)
    val repos: StateFlow<List<Repo>?> = _repos

    /** 加载状态流（控制加载指示器显示） */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** 错误信息状态流（显示网络请求错误） */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    init {
        // 初始化时自动加载默认仓库列表
        fetchRepositories()
    }

    /**
     * 根据编程语言搜索仓库
     * @param language 编程语言名称（如kotlin、java）
     * @param sort 排序方式（默认按星标数排序）
     */
    suspend fun search(language: String, sort: String = "stars") {
        val query = "language:$language"
        fetchRepositories(query, sort)
    }

    /**
     * 搜索仓库（内部方法）
     * @param query 搜索关键词（格式：key:value）
     * @param sort 排序字段（stars/updated）
     */
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

    /**
     * 获取公共仓库列表（默认方法）
     * 在ViewModel作用域内启动协程执行网络请求
     */
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