package com.hsbc.github

import android.app.Application
import android.content.Context

/**
 * 应用程序入口类
 * 负责全局应用状态管理和提供全局上下文访问
 */
class GitHubApp : Application() {
    companion object {
        /** 应用程序实例（单例模式） */
        private lateinit var instance: GitHubApp

        /**
         * 获取全局应用上下文
         * @return 应用程序上下文实例
         */
        fun applicationContext(): Context = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        // 初始化应用实例（供静态方法访问）
        instance = this
    }
}