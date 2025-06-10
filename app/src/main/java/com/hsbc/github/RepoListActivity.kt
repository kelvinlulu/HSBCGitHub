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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
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
import com.hsbc.github.MainActivity
import com.hsbc.github.model.Repo
import com.hsbc.github.model.Repository
import com.hsbc.github.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import kotlin.getValue

class RepoListActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()



    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 获取AuthCode
        val authCode = intent.getStringExtra("auth_code") ?: run {
            Log.e("RepoList", "未接收到AuthCode")
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                val response = authViewModel.fetchAndSaveAccessToken(authCode)
                // 2. 使用Token获取用户信息
                authViewModel.fetchUserInfo()
                authViewModel.fetchUserRepositories()


                Log.d("Auth", "Token: ${response.access_token}")
            } catch (e: Exception) {
                Log.e("Auth", "Error: ${e.message}")
            }
        }

        Log.d("RepoList", "接收到AuthCode: $authCode")

        setContent {
            val repos by authViewModel.repos.collectAsState(initial = null)
            val isLoading by authViewModel.isLoading.collectAsState(initial = false)
            val errorMessage by authViewModel.errorMessage.collectAsState(initial = null)
            val user by authViewModel.user.collectAsState(initial = null)

            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text("${user?.name}的仓库列表") // 显示"XXX的仓库列表"
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    // 点击返回按钮时关闭当前Activity
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
                            .padding(padding), // 避免内容被TopAppBar遮挡
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when {
                            repos != null -> RepoListScreen(repos!!, onItemClick = { repo ->
                                Intent(
                                    this@RepoListActivity,
                                    RepoDetailActivity::class.java
                                ).also { intent ->
                                    intent.putExtra("owner", user?.login ?: return@also)
                                    intent.putExtra("repoName", repo.name)
                                    intent.putExtra("token", authViewModel.getAccessToken())
                                    startActivity(intent)
                                }
                            })
                            else -> Text("暂无仓库数据", Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RepoListScreen(repos: List<Repository>, onItemClick: (Repository) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(repos.size) { index ->
            RepositoryItem(repo = repos[index], onItemClick = onItemClick)
        }
    }
}

@Composable
fun RepositoryItem(repo: Repository, onItemClick: (Repository) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onItemClick(repo) },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = repo.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            if (repo.description != null) {
                Text(
                    text = repo.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
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

