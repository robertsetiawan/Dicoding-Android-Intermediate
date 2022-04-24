package com.robertas.storyapp.models.domain

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "story")
data class Story(
    @PrimaryKey
    val id: String,

    val name: String,

    val description: String,

    val photoUrl: String,

    val createdAt: String,

    val lat : Double?,

    val lon: Double?
): Parcelable