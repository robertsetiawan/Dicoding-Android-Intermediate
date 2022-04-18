package com.robertas.storyapp

import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.domain.User

object DataDummy {
    fun generateUserDummy(): User {
        return User(
            userId = "abcd",
            name = "robertus agung",
            token = "quwuhibfasidf12",
        )
    }
}