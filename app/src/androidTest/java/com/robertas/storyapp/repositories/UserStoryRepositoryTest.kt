package com.robertas.storyapp.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.PagingSource
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.google.android.gms.maps.model.LatLng
import com.robertas.storyapp.DataDummy
import com.robertas.storyapp.FakeStoryService
import com.robertas.storyapp.StoryApp
import com.robertas.storyapp.abstractions.IDomainMapper
import com.robertas.storyapp.abstractions.IStoryService
import com.robertas.storyapp.abstractions.StoryDatabase
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.data.StoryPagingSource
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.network.StoryNetwork
import com.robertas.storyapp.utils.StoryNetworkMapper
import com.robertas.storyapp.utils.createTempFile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class UserStoryRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var userStoryRepository: StoryRepository

    private lateinit var apiService: IStoryService

    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var storyDatabase: StoryDatabase

    private lateinit var networkMapper: IDomainMapper<StoryNetwork, Story>

    @Before
    fun setUp() {

        val context = ApplicationProvider.getApplicationContext<StoryApp>()

        networkMapper = StoryNetworkMapper()

        apiService = FakeStoryService()

        storyDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            StoryDatabase::class.java
        ).build()


        sharedPreferences = context.getSharedPreferences(
            "user_setting_preferences",
            Context.MODE_PRIVATE
        )

        userStoryRepository = UserStoryRepository(apiService, networkMapper, sharedPreferences, storyDatabase)
    }

    @After
    fun tearDown() {
        sharedPreferences.edit().clear().apply()

        storyDatabase.clearAllTables()

        storyDatabase.close()
    }

    @Test
    fun getAllStoriesWithoutToken() = runBlocking{
        try {
            userStoryRepository.getAllStories(false)
        } catch (e: Exception){
            assertEquals(e.message, "Login Session has ended")
        }
        return@runBlocking
    }

    @Test
    fun getAllStoriesWithToken() = runBlocking {
        val user = DataDummy.generateUserDummy()

        val expectedStories = DataDummy.generateDummyStories(false)

        sharedPreferences.edit().apply {

            putString(USER_ID_KEY, user.userId)

            putString(USER_NAME_KEY, user.name)

            putString(USER_TOKEN_KEY, user.token)

            apply()
        }

        val actualStories = userStoryRepository.getAllStories(false)

        assertEquals(expectedStories.size, actualStories.size)

        assertEquals(expectedStories[0].id, actualStories[0].id)
    }

    @Test
    fun postStoryWithoutToken() = runBlocking{
        val file = createTempFile(ApplicationProvider.getApplicationContext<StoryApp>())

        try {
            userStoryRepository.postStory(file, "gambar", 0f)
        } catch (e: Exception){
            assertEquals(e.message, "Login Session has ended")
        }

        return@runBlocking
    }

    @Test
    fun postLocationStoryWithoutToken() = runBlocking {
        val file = createTempFile(ApplicationProvider.getApplicationContext<StoryApp>())

        try {
            userStoryRepository.postStory(file, "gambar", 0f, LatLng(0.0, 0.0))
        } catch (e: Exception){
            assertEquals(e.message, "Login Session has ended")
        }

        return@runBlocking
    }

    @Test
    fun postLocationStoryWithToken() = runBlocking {
        val file = File("src/test/resources/Cover.png")

        val user = DataDummy.generateUserDummy()

        sharedPreferences.edit().apply {

            putString(USER_ID_KEY, user.userId)

            putString(USER_NAME_KEY, user.name)

            putString(USER_TOKEN_KEY, user.token)

            apply()
        }

        val status = userStoryRepository.postStory(file, "gambar", 0f, LatLng(0.0, 0.0))

        assertTrue(status)
    }

    @Test
    fun postStoryWithToken() = runBlocking {
        val file = File("src/test/resources/Cover.png")

        val user = DataDummy.generateUserDummy()

        sharedPreferences.edit().apply {

            putString(USER_ID_KEY, user.userId)

            putString(USER_NAME_KEY, user.name)

            putString(USER_TOKEN_KEY, user.token)

            apply()
        }

        val status = userStoryRepository.postStory(file, "gambar", 0f)

        assertTrue(status)
    }

    @Test
    fun whenRefreshPaginatedDataIsSuccess() = runBlockingTest {
        val storyPagingSource = StoryPagingSource(apiService, networkMapper, sharedPreferences)

        val user = DataDummy.generateUserDummy()

        sharedPreferences.edit().apply {

            putString(USER_ID_KEY, user.userId)

            putString(USER_NAME_KEY, user.name)

            putString(USER_TOKEN_KEY, user.token)

            apply()
        }

        val expectedResult = PagingSource.LoadResult.Page(
            data = DataDummy.generateDummyStories(false),
            prevKey = null,
            nextKey = 2
        )

        assertEquals(
            expectedResult,
            storyPagingSource.load(
                PagingSource.LoadParams.Refresh(
                    key = 1,
                    loadSize = 1,
                    placeholdersEnabled = false
                )
            )
        )
    }

    companion object {
        const val USER_TOKEN_KEY = "user_token_key"

        const val USER_NAME_KEY = "user_name_key"

        const val USER_ID_KEY = "user_id_key"
    }
}