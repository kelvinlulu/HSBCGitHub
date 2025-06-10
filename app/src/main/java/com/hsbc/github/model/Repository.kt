package com.hsbc.github.model

import com.google.gson.annotations.SerializedName

// models/Repository.kt
data class Repository(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("stargazers_count") val starCount: Int
)