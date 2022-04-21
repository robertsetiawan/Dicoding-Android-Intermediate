package com.robertas.storyapp.abstractions

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.robertas.storyapp.models.domain.Story

class FakeStoryDao: StoryDao {

    private var stories = mutableListOf<Story>()

    override suspend fun insertAll(list: List<Story>) {
        stories.addAll(list)
    }

    override suspend fun deleteAll() {
        stories.clear()
    }

    override fun getAllStories(): PagingSource<Int, Story> {
        return PagedTestDataSources(stories)
    }
}

class PagedTestDataSources constructor(private val item: List<Story>) :
    PagingSource<Int, Story>() {

    override fun getRefreshKey(state: PagingState<Int, Story>): Int {
        return 0
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return LoadResult.Page(emptyList(), 0 , 1)
    }
}