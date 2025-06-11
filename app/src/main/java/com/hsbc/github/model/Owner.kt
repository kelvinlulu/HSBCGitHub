package com.hsbc.github.model

/**
 * 仓库所有者数据模型
 * 用于表示GitHub上仓库的拥有者信息
 */
data class Owner(
    /** 所有者的登录名（用户名） */
    val login: String
)