package com.hsbc.github.model

import com.google.gson.annotations.SerializedName

/**
 * GitHub仓库数据模型（完整版本）
 * 用于解析GitHub API返回的仓库信息，包含更多详细字段
 */
data class Repository(
    /** 仓库在GitHub上的唯一标识符（长整型） */
    @SerializedName("id") val id: Long,

    /** 仓库的名称（如"android"） */
    @SerializedName("name") val name: String,

    /** 仓库的详细描述，可能为null */
    @SerializedName("description") val description: String?,

    /** 仓库在GitHub上的网页链接（如"https://github.com/user/repo"） */
    @SerializedName("html_url") val htmlUrl: String,

    /** 仓库获得的星标数量（用户收藏数） */
    @SerializedName("stargazers_count") val starCount: Int
)