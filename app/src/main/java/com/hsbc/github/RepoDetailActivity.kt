package com.hsbc.github

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hsbc.github.model.Repo
import com.hsbc.github.viewmodel.RepoDetailViewModel

class RepoDetailActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("StateFlowValueCalledInComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: RepoDetailViewModel = viewModel()
            val owner = intent.getStringExtra("owner") ?: ""
            val repoName = intent.getStringExtra("repoName") ?: ""
            val isLoading by viewModel.isLoading.collectAsState(initial = false)
            val errorMessage by viewModel.errorMessage.collectAsState(initial = null)
            val repo by viewModel.repo.collectAsState(initial = null)

            LaunchedEffect(owner, repoName) {
                if (owner.isNotEmpty() && repoName.isNotEmpty()) {
                    viewModel.fetchRepoDetails(owner, repoName)
                }
            }

            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text("仓库详情")
                            },
                            navigationIcon = {
                                IconButton(onClick = {
                                    // 点击返回按钮时关闭当前Activity
                                    finish()
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "返回",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        )
                    }
                ) { padding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        when {
                            isLoading -> LoadingScreenInDetail()
                            errorMessage != null -> ErrorScreenInDetail(message = viewModel.errorMessage.value!!)
                            repo != null -> RepoDetailScreen(repo = viewModel.repo.value!!)
                            else -> EmptyScreen()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoDetailScreen(repo: Repo) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = repo.name) })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).padding(16.dp)
        ) {
            Text(
                text = repo.description ?: "暂无描述",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "作者是 ： ${repo.owner.login} ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star Count",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "${repo.stargazers_count} Stars",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

// 辅助组件（可单独定义）
@Composable
fun LoadingScreenInDetail() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreenInDetail(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, color = Color.Red)
    }
}

@Composable
fun EmptyScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "请选择一个仓库",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

