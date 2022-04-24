package com.robertas.storyapp.repositories

import androidx.paging.PagingSource
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.android.gms.maps.model.LatLng
import com.robertas.storyapp.DataDummy
import com.robertas.storyapp.FakeStoryService
import com.robertas.storyapp.abstractions.IDomainMapper
import com.robertas.storyapp.abstractions.IStoryService
import com.robertas.storyapp.abstractions.StoryDatabase
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.data.StoryPagingSource
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.models.network.StoryNetwork
import com.robertas.storyapp.utils.StoryNetworkMapper
import dagger.hilt.android.testing.HiltAndroidRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class UserStoryRepositoryTest {

    private lateinit var userStoryRepository: StoryRepository

    private lateinit var apiService: IStoryService

    private lateinit var storyDatabase: StoryDatabase

    private lateinit var networkMapper: IDomainMapper<StoryNetwork, Story>

    @Before
    fun setUp() {

        networkMapper = StoryNetworkMapper()

        apiService = FakeStoryService(false)

        storyDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StoryDatabase::class.java
        ).build()

        userStoryRepository = UserStoryRepository(apiService, networkMapper, storyDatabase)
    }

    @After
    fun tearDown() {

        storyDatabase.clearAllTables()

        storyDatabase.close()
    }

    @Test
    fun getAllStories() = runTest {

        val list = userStoryRepository.getAllStories("",false).toList()

        assertEquals(list, listOf(NetworkResult.Loading, NetworkResult.Success(DataDummy.generateDummyStories(false))))
    }

    @Test fun errorGetAllStories() = runTest {

        apiService = FakeStoryService(true)

        userStoryRepository = UserStoryRepository(apiService, networkMapper, storyDatabase)

        val list = userStoryRepository.getAllStories("",false).toList()

        assertEquals(list, listOf(NetworkResult.Loading, NetworkResult.Error("error")))
    }


    @Test
    fun postLocationStory() = runTest {
        val file = File("src/test/resources/Cover.png")

        val list = userStoryRepository.postStory("", file, "gambar", 0f, LatLng(0.0, 0.0)).toList()

        assertEquals(list, listOf(NetworkResult.Loading, NetworkResult.Success(true)))
    }

    @Test
    fun errorPostLocationStory() = runTest {

        apiService = FakeStoryService(true)

        userStoryRepository = UserStoryRepository(apiService, networkMapper, storyDatabase)

        val file = File("src/test/resources/Cover.png")

        val list = userStoryRepository.postStory("", file, "gambar", 0f, LatLng(0.0, 0.0)).toList()

        assertEquals(list, listOf(NetworkResult.Loading, NetworkResult.Error("error")))
    }

    @Test
    fun postStory() = runTest {
        val file = File("src/test/resources/Cover.png")

        val list = userStoryRepository.postStory("", file, "gambar", 0f).toList()

        assertEquals(list, listOf(NetworkResult.Loading, NetworkResult.Success(true)))
    }

    @Test
    fun errorPostStory() = runTest {

        apiService = FakeStoryService(true)

        userStoryRepository = UserStoryRepository(apiService, networkMapper, storyDatabase)

        val file = File("src/test/resources/Cover.png")

        val list = userStoryRepository.postStory("", file, "gambar", 0f).toList()

        assertEquals(list, listOf(NetworkResult.Loading, NetworkResult.Error("error")))
    }

    @Test
    fun whenRefreshPaginatedDataIsSuccess() = runTest {

        val user = DataDummy.generateUserDummy()

        val storyPagingSource = StoryPagingSource(apiService, networkMapper, user.token)

        val expectedResult = PagingSource.LoadResult.Page(
            data = DataDummy.generateDummyStories(false),
            prevKey = null,
            nextKey = 2
        )

        val actualResult = storyPagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 1,
                loadSize = 1,
                placeholdersEnabled = false
            )
        )

        assertEquals(
            expectedResult,
            actualResult
        )
    }

    @Test
    fun whenErrorRefreshPaginatedData() = runTest {

        val user = DataDummy.generateUserDummy()

        apiService = FakeStoryService(true)

        val storyPagingSource = StoryPagingSource(apiService, networkMapper, user.token)

        val expectedResult = PagingSource.LoadResult.Page(
            data = listOf(),
            prevKey = null,
            nextKey = null
        )

        val actualResult = storyPagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = 1,
                loadSize = 1,
                placeholdersEnabled = false
            )
        )

        assertEquals(
            expectedResult,
            actualResult
        )
    }
}