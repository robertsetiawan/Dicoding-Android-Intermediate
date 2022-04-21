package com.robertas.storyapp.models.network

import com.robertas.storyapp.abstractions.ApiResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserResponse(
    @Json(name = "message")
    override val message: String,

    @Json(name = "error")
    override val error: Boolean,

    @Json(name = "loginResult")
    override val data: UserNetwork?
): ApiResponse<UserNetwork>()