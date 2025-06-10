@file:OptIn(ExperimentalMaterial3Api::class)

package com.hsbc.github

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue  // 确保导入此包！
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hsbc.github.model.Repo
import com.hsbc.github.network.RetrofitClient
import com.hsbc.github.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    fun launchLogin() {
        val clientId = "Iv23lizoSx3TJhhTZOom"
        val authUrl = "https://github.com/login/oauth/authorize?" +
                "client_id=$clientId&" +
                "redirect_uri=myapp://callback&" +
                "scope=repo"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        intent.`package` = "com.android.chrome"
        startActivity(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent called with intent: " + intent)
        val uri = intent.data
        if (uri?.scheme == "myapp" && uri.host == "callback") {
            val authCode = uri.getQueryParameter("code") ?: return

            val intent = Intent(this, RepoListActivity::class.java)
            intent.putExtra("auth_code", authCode)
            startActivity(intent)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val viewModel: MainViewModel = viewModel()

            // 将状态收集移到 @Composable 函数中
            val repos by viewModel.repos.collectAsState(initial = null)
            val isLoading by viewModel.isLoading.collectAsState(initial = false)
            val errorMessage by viewModel.errorMessage.collectAsState(initial = null)
            val activityScope = rememberCoroutineScope()

            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text("汇丰银行GitHubApp")
                            },
                        )
                    }
                ) { padding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding), // 添加内边距，避免内容被TopAppBar遮挡
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Column {
                            SearchBar { language, sort ->
                                activityScope.launch {
                                    viewModel.search(language, sort)
                                }
                            }

                            // 登录按钮保持不变
                            Button(
                                onClick = {
                                    this@MainActivity.launchLogin()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text("登录 GitHub")
                            }

                            // 内容区域保持不变
                            when {
                                isLoading -> LoadingScreen()
                                errorMessage != null -> ErrorScreen(errorMessage = errorMessage!!)
                                repos != null -> RepoListScreen(
                                    repos = repos!!,
                                    onItemClick = { repo ->
                                        Intent(
                                            this@MainActivity,
                                            RepoDetailActivity::class.java
                                        ).also { intent ->
                                            intent.putExtra("owner", repo.owner.login)
                                            intent.putExtra("repoName", repo.name)
                                            startActivity(intent)
                                        }
                                    }
                                )
                                else -> Text("暂无数据", Modifier.padding(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // 临时测试网络请求（正式开发应使用 ViewModel）
        lifecycleScope.launch {
            try {
                Log.d("GITHUB_REPOS", "开始")
                val repos = RetrofitClient.api.getRepositories()
                Log.d("GITHUB_REPOS", "成功获取 ${repos.size} 个仓库")
            } catch (e: Exception) {
                Log.e("GITHUB_REPOS", "网络请求失败：${e.message}")
            }
        }
    }
}



@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("加载中...")
    }
}

@Composable
fun ErrorScreen(errorMessage: String) {
    Text(
        text = errorMessage,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(16.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun RepoListScreen(repos: List<Repo>, onItemClick: (Repo) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(repos.size) { index ->
            RepoItem(repo = repos[index], onItemClick = onItemClick)
        }
    }
}

@Composable
fun RepoItem(repo: Repo, onItemClick: (Repo) -> Unit) {
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
                    text = "${repo.stargazers_count} Stars",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}


@Composable
fun SearchBar(onSearch: (String, String) -> Unit) {
    var language by remember { mutableStateOf("kotlin") }
    var sort by remember { mutableStateOf("stars") }
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 语言输入框
        TextField(
            value = language,
            onValueChange = { language = it },
            label = { Text("输入编程语言") },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(16.dp))

        // 排序下拉菜单（使用 ExposedDropdownMenuBox）
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = when (sort) {
                    "stars" -> "星级"
                    "updated" -> "更新时间"
                    else -> "排序方式"
                },
                onValueChange = {},
                label = { Text("排序") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .width(100.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                // 星级排序选项
                DropdownMenuItem(
                    text = { Text("星级") },
                    leadingIcon = { Icon(Icons.Default.Star, null) },
                    onClick = {
                        sort = "stars"
                        expanded = false
                    }
                )

                // 更新时间排序选项
                DropdownMenuItem(
                    text = { Text("更新时间") },
                    leadingIcon = { Icon(Icons.Default.Star, null) },
                    onClick = {
                        sort = "updated"
                        expanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = { onSearch(language, sort) },
            modifier = Modifier.height(56.dp) // 与输入框高度匹配
        ) {
            Text("搜索")
        }
    }
}