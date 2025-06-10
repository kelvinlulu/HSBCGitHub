package com.hsbc.github.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsbc.github.model.Repo
import com.hsbc.github.network.GitHubApi
import com.hsbc.github.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RepoDetailViewModel : ViewModel() {
    private val _repo = MutableStateFlow<Repo?>(null)
    val repo: StateFlow<Repo?> = _repo

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun fetchRepoDetails(owner: String, repoName: String) {
        viewModelScope.launch {
            Log.d("RepoDetail", "开始请求：$owner/$repoName") // 新增日志
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val result = RetrofitClient.api.getRepoDetails(owner, repoName)
                Log.d("RepoDetail", "结束请求：${result.name}") // 新增日志
                _repo.value = result
                Log.d("RepoDetail", "结束请求：${_repo.value?.name}") // 新增日志
            } catch (e: Exception) {
                _errorMessage.value = "加载失败: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}