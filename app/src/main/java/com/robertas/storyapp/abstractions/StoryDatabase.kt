package com.robertas.storyapp.abstractions

import androidx.room.Database
import androidx.room.RoomDatabase
import com.robertas.storyapp.models.domain.RemoteKeys
import com.robertas.storyapp.models.domain.Story

@Database(entities = [Story::class, RemoteKeys::class], version = 1, exportSchema = false)
abstract class StoryDatabase: RoomDatabase() {

    abstract val remoteKeysDao: RemoteKeysDao

    abstract val storyDao: StoryDao

    companion object {
        const val DATABASE_NAME = "story_db"
    }
}