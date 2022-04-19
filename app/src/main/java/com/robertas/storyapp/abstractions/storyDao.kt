package com.robertas.storyapp.abstractions

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import com.robertas.storyapp.models.domain.Story

@Dao
interface StoryDao: BaseDao<Story> {

    @Query("DELETE FROM story")
    override suspend fun deleteAll()

    @Query("SELECT * FROM story")
    fun getAllStories(): PagingSource<Int, Story>
}