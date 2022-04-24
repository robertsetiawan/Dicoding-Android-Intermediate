package com.robertas.storyapp.utils

import com.robertas.storyapp.abstractions.IDomainMapper
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.network.StoryNetwork
import javax.inject.Inject

class StoryNetworkMapper @Inject constructor(): IDomainMapper<StoryNetwork, Story> {
    override fun mapToEntity(source: StoryNetwork): Story {
        return Story(
            id = source.id,
            name = source.name,
            description = source.description,
            photoUrl = source.photoUrl,
            createdAt = parseTime(source.createdAt)?.let { formatTime(it.time, DATETIME_UI_FORMAT) }
                ?: "",
            lat = source.lat,
            lon = source.lon
        )
    }
}