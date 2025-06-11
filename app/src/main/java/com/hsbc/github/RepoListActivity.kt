package com.hsbc.github

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.hsbc.github.model.Repository
import com.hsbc.github.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

/**
 * 仓库列表活动类
 * 用于展示当前登录用户的GitHub仓库列表
 */
class RepoListActivity : ComponentActivity() {
    // 获取认证视图模型实例，用于获取用户仓库数据
    private val authViewModel: AuthViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 在协程中调用视图模型获取用户仓库数据
        lifecycleScope.launch {
            try {
                authViewModel.fetchUserRepositories()
            } catch (e: Exception) {
                Log.e("Auth", "Error: ${e.message}")
            }
        }
        setContent {
            // 收集仓库数据状态（初始值为null）
            val repos by authViewModel.repos.collectAsState(initial = null)
            // 收集加载状态（控制进度指示器显示）
            val isLoading by authViewModel.isLoading.collectAsState(initial = false)
            // 收集错误信息（网络请求失败时显示）
            val errorMessage by authViewModel.errorMessage.collectAsState(initial = null)

            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                // 显示"用户登录名的仓库列表"
                                Text("${authViewModel.getUserLogin() }的仓库列表")
                            },
                            navigationIcon = {
                                // 顶部栏返回按钮，点击关闭当前页面
                                IconButton(onClick = {
                                    finish()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "返回"
                                    )
                                }
                            }
                        )
                    }
                ) { padding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding), // 添加内边距避免顶部栏遮挡内容
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // 根据不同状态显示对应界面
                        when {
                            isLoading -> LoadingScreen() // 加载中状态
                            errorMessage != null -> ErrorScreen(errorMessage!!) // 错误状态
                            repos != null -> RepoListScreen(repos!!, onItemClick = { repo ->
                                // 点击仓库项时跳转到详情页
                                Intent(
                                    this@RepoListActivity,
                                    RepoDetailActivity::class.java
                                ).also { intent ->
                                    intent.putExtra("owner", authViewModel.getUserLogin())
                                    intent.putExtra("repoName", repo.name)
                                    intent.putExtra("token", authViewModel.getAccessToken())
                                    startActivity(intent)
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}

/**
 * 仓库列表组件
 * 展示所有仓库项的懒加载列表
 */
@Composable
fun RepoListScreen(repos: List<Repository>, onItemClick: (Repository) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(repos.size) { index ->
            // 渲染单个仓库项组件
            RepositoryItem(repo = repos[index], onItemClick = onItemClick)
        }
    }
}

/**
 * 仓库项组件
 * 展示单个仓库的名称、描述和星标数
 */
@Composable
fun RepositoryItem(repo: Repository, onItemClick: (Repository) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onItemClick(repo) }, // 点击事件回调
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 仓库名称（加粗显示）
            Text(
                text = repo.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            // 仓库描述（非空时显示）
            if (repo.description != null) {
                Text(
                    text = repo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            // 星标数（带星星图标）
            Row(
                modifier = Modifier.padding(top = 16.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star Count",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "${repo.starCount} Stars",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}