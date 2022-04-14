package com.robertas.storyapp.models.domain

data class Story(
    val id: String,

    val name: String,

    val description: String,

    val photoUrl: String,

    val createdAt: String,
)