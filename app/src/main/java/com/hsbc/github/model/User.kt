package com.hsbc.github.model

import com.google.gson.annotations.SerializedName

/**
 * GitHub用户数据模型
 * 用于解析GitHub API返回的用户信息，包含用户基本资料
 */
data class User(
    /** 用户的登录名（唯一标识符，如"octocat"） */
    @SerializedName("login") val login: String,

    /** 用户在GitHub上的唯一数字ID */
    @SerializedName("id") val id: Long,

    /** 用户头像的URL地址 */
    @SerializedName("avatar_url") val avatarUrl: String,

    /** 用户的真实姓名，可能为null（如未设置） */
    @SerializedName("name") val name: String?
)