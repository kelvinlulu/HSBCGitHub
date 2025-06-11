package com.hsbc.github.utils

import android.content.Context
import androidx.core.content.edit

/**
 * 安全存储工具类
 * 用于存储和管理用户认证信息及个人资料
 */
class SecureStorage(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(
        "auth_data", Context.MODE_PRIVATE
    )

    /** 保存访问令牌 */
    fun saveAccessToken(token: String) {
        sharedPreferences.edit { putString("access_token", token) }
    }

    /** 获取访问令牌 */
    fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    /** 保存用户真实姓名 */
    fun saveUserName(userName: String) {
        sharedPreferences.edit { putString("user_name", userName) }
    }

    /** 获取用户真实姓名 */
    fun getUserName(): String? {
        return sharedPreferences.getString("user_name", null)
    }

    /** 保存用户登录名 */
    fun saveUserLogin(login: String) {
        sharedPreferences.edit { putString("user_login", login) }
    }

    /** 获取用户登录名 */
    fun getUserLogin(): String? {
        return sharedPreferences.getString("user_login", null)
    }

    /** 清除所有用户数据（用于退出登录） */
    fun clearAllUserData() {
        sharedPreferences.edit {
            remove("access_token")
            remove("user_name")
            remove("user_login")
        }
    }
}