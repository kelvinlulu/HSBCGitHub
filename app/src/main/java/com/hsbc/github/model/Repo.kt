package com.hsbc.github.model

data class Repo(
    val id: Int,
    val name: String,
    val owner: Owner,
    val description: String?,
    val stargazers_count: Int
)

data class Owner(
    val login: String
)



