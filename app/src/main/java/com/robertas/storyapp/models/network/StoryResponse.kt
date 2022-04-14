package com.robertas.storyapp.models.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StoryResponse(
    @Json(name = "message")
    override val message: String,

    @Json(name = "error")
    override val error: Boolean,

    @Json(name = "listStory")
    override val data: List<StoryNetwork>?
): ApiResponse<List<StoryNetwork>>()
