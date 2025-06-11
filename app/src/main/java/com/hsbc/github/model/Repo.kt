package com.hsbc.github.model

/**
 * GitHub仓库数据模型
 * 用于表示GitHub上的一个代码仓库
 */
data class Repo(
    /** 仓库唯一标识符 */
    val id: Int,

    /** 仓库名称 */
    val name: String,

    /** 仓库所有者信息 */
    val owner: Owner,

    /** 仓库描述信息，可能为空 */
    val description: String?,

    /** 仓库的星标数量 */
    val stargazers_count: Int
)

