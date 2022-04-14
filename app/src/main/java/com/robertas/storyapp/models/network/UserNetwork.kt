package com.robertas.storyapp.models.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserNetwork(
    val userId: String,

    val name: String,

    val token: String
)
