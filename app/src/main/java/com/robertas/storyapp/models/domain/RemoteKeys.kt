package com.robertas.storyapp.models.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_key")
data class RemoteKeys(
    @PrimaryKey
    val id: String,

    val prevKey: Int?,

    val nextKey: Int?
)
