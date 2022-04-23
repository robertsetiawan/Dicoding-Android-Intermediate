package com.robertas.storyapp.viewmodels

import android.location.Location
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.google.android.gms.maps.model.LatLng
import com.robertas.storyapp.CoroutinesTestRule
import com.robertas.storyapp.DataDummy
import com.robertas.storyapp.MainCoroutineRule
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.adapters.StoryListAdapter
import com.robertas.storyapp.getOrAwaitValue
import com.robertas.storyapp.models.enums.CameraMode
import com.robertas.storyapp.models.enums.NetworkResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class StoryViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var userStoryRepository: StoryRepository

    @Mock
    private lateinit var userAccountRepository: UserRepository

    private lateinit var storyViewModel: StoryViewModel

    private val dummyToken = DataDummy.generateDummyToken()

    @Before
    fun setUp() {
        storyViewModel = StoryViewModel(userStoryRepository, userAccountRepository)
    }

    @Test
    fun `when Get Paginated Story should not null`() = runBlocking {
        val dummyData = DataDummy.generateDummyStories(false)

        val data = PagingData.from(dummyData)

        `when`(userAccountRepository.getBearerToken()).thenReturn(dummyToken)

        `when`(userStoryRepository.getAllStories(dummyToken)).thenReturn(flowOf(data))

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryListAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            mainDispatcher = coroutinesTestRule.testDispatcher,
            workerDispatcher = coroutinesTestRule.testDispatcher,
        )

        val pagingData = storyViewModel.getPaginatedData().getOrAwaitValue()

        differ.submitData(pagingData)

        Mockito.verify(userStoryRepository).getAllStories(dummyToken)

        assertNotNull(differ.snapshot())

        assertEquals(dummyData.size, differ.snapshot().size)

        assertEquals(dummyData[0].id, differ.snapshot()[0]?.id)
    }

    @Test
    fun `when validate story then story is valid`() {
        storyViewModel.validateStories()

        val actualIsStoryValid = storyViewModel.isStoriesInvalid.getOrAwaitValue()

        assertFalse(actualIsStoryValid)
    }

    @Test
    fun `when invalidate story then story is invalid`() {
        storyViewModel.invalidateStories()

        val actualIsStoryValid = storyViewModel.isStoriesInvalid.getOrAwaitValue()

        assertTrue(actualIsStoryValid)
    }

    @Test
    fun `when upload image then error is false`() = runTest {
        val file = File("src/test/resources/Cover.png")

        `when`(userAccountRepository.getBearerToken()).thenReturn(dummyToken)

        `when`(userStoryRepository.postStory(dummyToken, file, "desc", 0f)).thenReturn(flowOf(NetworkResult.Success(true)))

        storyViewModel.uploadImage(file, "desc", 0f).collect { result ->

            assert(result is NetworkResult.Success)

            assertTrue((result as NetworkResult.Success).data)

            Mockito.verify(userStoryRepository).postStory(dummyToken, file, "desc", 0f)
        }
    }


    @Test
    fun `when error upload image then result is error`() = runTest {
        val file = File("src/test/resources/Cover.png")

        `when`(userAccountRepository.getBearerToken()).thenReturn(dummyToken)

        `when`(userStoryRepository.postStory(dummyToken, file, "desc", 0f)).thenReturn(flowOf(NetworkResult.Error("data harus lengkap")))

        storyViewModel.uploadImage(file, "desc", 0f).collect { result ->

            assert(result is NetworkResult.Error)

            assertEquals("data harus lengkap", (result as NetworkResult.Error).message)

            Mockito.verify(userStoryRepository).postStory(dummyToken, file, "desc", 0f)
        }
    }


    @Test
    fun `when success upload story with location then error is false`() = runTest {
        val file = File("src/test/resources/Cover.png")

        val location = LatLng(0.0, 0.0)

        `when`(userAccountRepository.getBearerToken()).thenReturn(dummyToken)

        `when`(userStoryRepository.postStory(dummyToken, file, "desc", 0f, location)).thenReturn(flowOf(NetworkResult.Success(true)))

        storyViewModel.uploadImage(file, "desc", 0f, location).collect { result ->

            assert(result is NetworkResult.Success)

            assertTrue((result as NetworkResult.Success).data)

            Mockito.verify(userStoryRepository).postStory(dummyToken, file, "desc", 0f, location)
        }
    }


    @Test
    fun `when error upload story with location then error is true`() = runTest {
        val file = File("src/test/resources/Cover.png")

        val location = LatLng(0.0, 0.0)

        `when`(userAccountRepository.getBearerToken()).thenReturn(dummyToken)

        `when`(userStoryRepository.postStory(dummyToken, file, "desc", 0f, location)).thenReturn(flowOf(NetworkResult.Error("lokasi tidak ditemukan")))

        storyViewModel.uploadImage(file, "desc", 0f, location).collect { result ->

            assert(result is NetworkResult.Error)

            assertEquals("lokasi tidak ditemukan", (result as NetworkResult.Error).message)

            Mockito.verify(userStoryRepository).postStory(dummyToken, file, "desc", 0f, location)
        }
    }


    @Test
    fun `get camera mode return saved camera mode`() {
        val expectedCameraMode = CameraMode.CAMERA_X

        `when`(userAccountRepository.getCameraMode()).thenReturn(expectedCameraMode)

        storyViewModel.setCameraMode(expectedCameraMode)

        val actualCameraMode = storyViewModel.getCameraMode()

        assertEquals(expectedCameraMode, actualCameraMode)

        Mockito.verify(userAccountRepository).getCameraMode()

        Mockito.verify(userAccountRepository).setCameraMode(expectedCameraMode)
    }
}


val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}