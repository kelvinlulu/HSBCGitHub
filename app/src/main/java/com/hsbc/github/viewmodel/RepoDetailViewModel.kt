package com.hsbc.github.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsbc.github.model.Repo
import com.hsbc.github.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * 仓库详情视图模型
 * 负责获取和管理单个GitHub仓库的详细信息
 */
class RepoDetailViewModel : ViewModel() {
    /** 仓库详情状态流（包含完整仓库数据） */
    private val _repo = MutableStateFlow<Repo?>(null)
    val repo: StateFlow<Repo?> = _repo

    /** 加载状态流（控制加载指示器显示） */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    /** 错误信息状态流（显示网络请求错误） */
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    /**
     * 获取仓库详情
     * @param owner 仓库所有者登录名
     * @param repoName 仓库名称
     */
    fun fetchRepoDetails(owner: String, repoName: String) {
        viewModelScope.launch {
            Log.d("RepoDetail", "开始请求：$owner/$repoName") // 记录请求开始
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = RetrofitClient.api.getRepoDetails(owner, repoName)
                _repo.value = result
                Log.d("RepoDetail", "结束请求：${_repo.value?.name}") // 记录请求成功
            } catch (e: Exception) {
                _errorMessage.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}