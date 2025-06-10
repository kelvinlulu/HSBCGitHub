package com.hsbc.github.utils

import android.content.Context
import androidx.core.content.edit

// utils/SecureStorage.kt
class SecureStorage(context: Context) {
    private val sharedPreferences = context.getSharedPreferences(
        "auth_data", Context.MODE_PRIVATE
    )

    fun saveAccessToken(token: String) {
        sharedPreferences.edit { putString("access_token", token) }
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    fun clearAccessToken() {
        sharedPreferences.edit { remove("access_token") }
    }
}