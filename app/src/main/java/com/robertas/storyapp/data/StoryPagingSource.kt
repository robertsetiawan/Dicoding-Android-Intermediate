package com.robertas.storyapp.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.robertas.storyapp.abstractions.IDomainMapper
import com.robertas.storyapp.abstractions.IStoryService
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.network.StoryNetwork
import javax.inject.Inject

class StoryPagingSource @Inject constructor(
    private val apiService: IStoryService,
    private val networkMapper: IDomainMapper<StoryNetwork, Story>,
    private val token: String
) : PagingSource<Int, Story>() {
    override fun getRefreshKey(state: PagingState<Int, Story>): Int? {

        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Story> {
        return try {

            val page = params.key ?: INITIAL_PAGE_KEY

            val response = apiService.getAllStories(token, 0, page, params.loadSize)

            LoadResult.Page(
                data = response.data?.map { networkMapper.mapToEntity(it) }.orEmpty(),
                prevKey = if (page == 1) null else page - 1,
                nextKey = if (response.data.isNullOrEmpty()) null else page + 1
            )

        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    companion object {
        private const val INITIAL_PAGE_KEY = 1

    }
}