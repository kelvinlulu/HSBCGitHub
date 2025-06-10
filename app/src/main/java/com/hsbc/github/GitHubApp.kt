package com.hsbc.github

import android.app.Application
import android.content.Context

// MyApplication.kt
class GitHubApp : Application() {
    companion object {
        private lateinit var instance: GitHubApp

        fun applicationContext(): Context = instance.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

    }

}