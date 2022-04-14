package com.robertas.storyapp.utils

import com.robertas.storyapp.abstractions.IDomainMapper
import com.robertas.storyapp.models.domain.User
import com.robertas.storyapp.models.network.UserNetwork
import javax.inject.Inject

class UserMapper @Inject constructor(): IDomainMapper<UserNetwork, User> {
    override fun mapToEntity(source: UserNetwork): User {
        return User(
            userId = source.userId,
            name = source.name,
            token = source.token
        )
    }
}