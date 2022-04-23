package com.robertas.storyapp.data

import android.content.SharedPreferences
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.robertas.storyapp.abstractions.*
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.domain.RemoteKeys
import com.robertas.storyapp.models.network.StoryNetwork

@OptIn(ExperimentalPagingApi::class)
class StoryRemoteMediator(
    private val token: String,
    private val storyDatabase: StoryDatabase,
    private val apiService: IStoryService,
    private val storyNetworkMapper: IDomainMapper<StoryNetwork, Story>
) : RemoteMediator<Int, Story>() {

    override suspend fun initialize(): InitializeAction {
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(loadType: LoadType, state: PagingState<Int, Story>): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)

                remoteKeys?.nextKey?.minus(1) ?: INITIAL_PAGE_KEY
            }

            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)

                val prevKey = remoteKeys?.prevKey

                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)

                prevKey
            }

            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)

                val nextKey = remoteKeys?.nextKey
                    ?: return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                nextKey
            }
        }

        try {

            val response = apiService.getAllStories(token, 0, page, state.config.pageSize)

            val listStory =
                response.data?.map { storyNetworkMapper.mapToEntity(it) }.orEmpty()

            val endOfPaginationReached = listStory.isEmpty()

            storyDatabase.withTransaction {

                if (loadType == LoadType.REFRESH) {
                    storyDatabase.remoteKeysDao.deleteAll()

                    storyDatabase.storyDao.deleteAll()
                }

                val prevKey = if (page == 1) null else page - 1

                val nextKey = if (endOfPaginationReached) null else page + 1

                val keys = listStory.map {
                    RemoteKeys(id = it.id, prevKey = prevKey, nextKey = nextKey)
                }

                storyDatabase.remoteKeysDao.insertAll(keys)

                storyDatabase.storyDao.insertAll(listStory)
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)

        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, Story>): RemoteKeys? {
        return state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()?.let { data ->
            storyDatabase.remoteKeysDao.getRemoteKeysFromId(data.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, Story>): RemoteKeys? {
        return state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { data ->
            storyDatabase.remoteKeysDao.getRemoteKeysFromId(data.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(state: PagingState<Int, Story>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { id ->
                storyDatabase.remoteKeysDao.getRemoteKeysFromId(id)
            }
        }
    }

    companion object {
        private const val INITIAL_PAGE_KEY = 1
    }
}