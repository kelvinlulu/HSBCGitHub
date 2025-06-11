package com.hsbc.github

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.hsbc.github.model.Repo
import com.hsbc.github.model.User
import com.hsbc.github.network.RetrofitClient
import com.hsbc.github.viewmodel.AuthViewModel
import com.hsbc.github.viewmodel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 主活动类
 * 应用程序的主要入口点，负责管理UI界面和用户交互
 */
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    // 认证视图模型，管理用户登录状态和认证逻辑
    private val authViewModel: AuthViewModel by viewModels()

    /**
     * 启动GitHub登录流程
     * 打开浏览器跳转到GitHub授权页面
     */
    fun launchLogin() {
        val clientId = "Iv23lizoSx3TJhhTZOom"
        val authUrl = "https://github.com/login/oauth/authorize?" +
                "client_id=$clientId&" +
                "redirect_uri=myapp://callback&" +
                "scope=repo"

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
        startActivity(intent)
    }

    /**
     * 返回主页（当前活动已为主页，此处为示例）
     */
    fun goToRepoListActivity() {
        lifecycleScope.launch {
            // 获取用户信息
            authViewModel.fetchUserInfo()
            Log.d("MainActivity", "go to RepoListActivity")
            // 跳转到仓库列表页面
            val intent = Intent(this@MainActivity, RepoListActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * 处理返回的Intent（来自GitHub授权回调）
     * 提取授权码并完成登录流程
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri = intent.data
        if (uri?.scheme == "myapp" && uri.host == "callback") {
            val authCode = uri.getQueryParameter("code") ?: return
            lifecycleScope.launch {
                // 使用授权码获取访问令牌
                authViewModel.fetchAndSaveAccessToken(authCode)
                // 获取用户信息
                authViewModel.fetchUserInfo()
                Log.d("MainActivity", "go to RepoListActivity")
                // 跳转到仓库列表页面
                val intent = Intent(this@MainActivity, RepoListActivity::class.java)
                startActivity(intent)
            }
        }
    }

    /**
     * 活动创建时调用
     * 初始化界面和视图模型
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // 初始化主视图模型，管理仓库数据
            val viewModel: MainViewModel = viewModel()
            // 收集仓库数据状态
            val repos by viewModel.repos.collectAsState(initial = null)
            // 收集加载状态
            val isLoading by viewModel.isLoading.collectAsState(initial = false)
            // 收集错误信息状态
            val errorMessage by viewModel.errorMessage.collectAsState(initial = null)
            // 创建协程作用域
            val activityScope = rememberCoroutineScope()
            // 收集登录状态
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState(false)
            // 用户信息状态
            val user by authViewModel.user.collectAsState(null)

            // 构建主应用界面
            MainAppUI(
                activity = this,
                viewModel = viewModel,
                authViewModel = authViewModel,
                repos = repos,
                isLoading = isLoading,
                errorMessage = errorMessage,
                activityScope = activityScope,
                isLoggedIn = isLoggedIn,
                user = user
            )
        }

        // 临时测试网络请求（正式开发中应通过ViewModel处理）
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

/**
 * 主应用界面组件
 * 组合所有子组件构建完整页面
 */
@Composable
fun MainAppUI(
    activity: MainActivity,                // 当前活动引用，用于页面跳转
    viewModel: MainViewModel,          // 主视图模型，管理仓库数据
    authViewModel: AuthViewModel,      // 认证视图模型，管理登录状态
    repos: List<Repo>?,                // 仓库列表数据
    isLoading: Boolean,                // 加载状态
    errorMessage: String?,             // 错误信息
    activityScope: CoroutineScope,     // 协程作用域，用于执行异步操作
    isLoggedIn: Boolean,                // 登录状态
    user: User?
) {
    MaterialTheme {
        // 脚手架布局，包含顶部栏和内容区域
        Scaffold(
            topBar = {
                // 顶部导航栏组件，包含主页、登录/退出按钮
                MainTopAppBar(
                    isLoggedIn = isLoggedIn,
                    userName = user?.name ?: authViewModel.getUserName(),
                    onLoginClick = { activity.launchLogin() },
                    onLogoutClick = { authViewModel.logout() },
                    onHomeClick = { activity.goToRepoListActivity() }
                )
            }
        ) { padding ->
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),  // 添加内边距避免内容被顶部栏遮挡
                color = MaterialTheme.colorScheme.background
            ) {
                Column {
                    // 搜索栏组件，用于搜索特定语言的仓库
                    SearchBar { language, sort ->
                        activityScope.launch {
                            viewModel.search(language, sort)
                        }
                    }

                    // 主内容区域组件，根据不同状态显示加载中、错误或仓库列表
                    MainContentArea(
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        repos = repos,
                        onItemClick = { repo ->
                            Intent(activity, RepoDetailActivity::class.java).also { intent ->
                                intent.putExtra("owner", repo.owner.login)
                                intent.putExtra("repoName", repo.name)
                                activity.startActivity(intent)
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * 顶部导航栏组件
 * 显示应用标题、主页按钮和登录/退出按钮（右上角）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopAppBar(
    isLoggedIn: Boolean,                // 登录状态
    userName: String,                  // 用户名
    onLoginClick: () -> Unit,          // 登录点击回调
    onLogoutClick: () -> Unit,         // 退出点击回调
    onHomeClick: () -> Unit            // 主页点击回调
) {
    TopAppBar(
        title = { Text("HSBC\nGitHub") },
        actions = {
            // 根据登录状态显示不同按钮
            if (isLoggedIn) {
                // 已登录：显示用户名和退出按钮
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Button(
                        onClick = onHomeClick,
                        modifier = Modifier
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "$userName 的主页")
                        }
                    }
                    Button(
                        onClick = onLogoutClick,
                        modifier = Modifier
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "退出")
                        }
                    }
                }
            } else {
                // 未登录：显示登录按钮
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = "登录")
                }
            }
        }
    )
}

/**
 * 主内容区域组件
 * 根据不同状态显示加载中、错误信息或仓库列表
 */
@Composable
fun MainContentArea(
    isLoading: Boolean,                // 加载状态
    errorMessage: String?,             // 错误信息
    repos: List<Repo>?,                // 仓库列表数据
    onItemClick: (Repo) -> Unit        // 仓库项点击回调
) {
    when {
        isLoading -> LoadingScreen()          // 显示加载中界面
        errorMessage != null -> ErrorScreen(errorMessage = errorMessage)  // 显示错误信息
        repos != null -> RepoListScreen(      // 显示仓库列表
            repos = repos,
            onItemClick = onItemClick
        )
        else -> Text("暂无数据", Modifier.padding(16.dp))  // 显示空状态
    }
}

/**
 * 加载中界面组件
 * 显示进度指示器和提示文本
 */
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("加载中...")
        }
    }
}

/**
 * 错误信息界面组件
 * 显示错误提示文本
 */
@Composable
fun ErrorScreen(errorMessage: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 仓库列表界面组件
 * 使用懒加载列表显示仓库项
 */
@Composable
fun RepoListScreen(repos: List<Repo>, onItemClick: (Repo) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(repos.size) { index ->
            RepoItem(repo = repos[index], onItemClick = onItemClick)
        }
    }
}

/**
 * 仓库项组件
 * 显示单个仓库的名称、描述和星标数
 */
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

/**
 * 搜索栏组件
 * 提供语言搜索和排序功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(onSearch: (String, String) -> Unit) {
    var language by remember { mutableStateOf("kotlin") }  // 搜索的编程语言
    var sort by remember { mutableStateOf("stars") }       // 排序方式
    var expanded by remember { mutableStateOf(false) }      // 下拉菜单展开状态

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

        // 排序下拉菜单
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
            modifier = Modifier.height(56.dp)  // 与输入框高度匹配
        ) {
            Text("搜索")
        }
    }
}