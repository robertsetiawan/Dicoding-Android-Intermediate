package com.robertas.storyapp.models.network

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StoryNetwork(
    val id: String,

    val name: String,

    val description: String,

    val photoUrl: String,

    val createdAt: String,

    val lat : Double?,

    val lon: Double?
)
