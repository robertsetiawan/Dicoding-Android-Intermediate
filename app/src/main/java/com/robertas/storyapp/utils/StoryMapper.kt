package com.robertas.storyapp.utils

import com.robertas.storyapp.abstractions.IDomainMapper
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.network.StoryNetwork
import javax.inject.Inject

class StoryMapper @Inject constructor(): IDomainMapper<StoryNetwork, Story> {
    override fun mapToEntity(source: StoryNetwork): Story {
        return Story(
            id = source.id,
            name = source.name,
            description = source.description,
            photoUrl = source.photoUrl,
            createdAt = source.createdAt
        )
    }
}