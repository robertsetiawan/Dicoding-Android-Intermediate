package com.robertas.storyapp.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.robertas.storyapp.DataDummy
import com.robertas.storyapp.MainCoroutineRule
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.adapters.StoryListAdapter
import com.robertas.storyapp.getOrAwaitValue
import com.robertas.storyapp.models.domain.Story
import com.robertas.storyapp.models.enums.CameraMode
import com.robertas.storyapp.models.enums.NetworkResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
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
    var mainCoroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var userStoryRepository: StoryRepository

    @Mock
    private lateinit var userAccountRepository: UserRepository

    private lateinit var storyViewModel: StoryViewModel

    @Before
    fun setUp() {
        storyViewModel = StoryViewModel(userStoryRepository, userAccountRepository)
    }

    @Test
    fun `when finish navigating then upload state is loading`() {
        storyViewModel.doneNavigating()

        val actualUploadState = storyViewModel.uploadStoryState.getOrAwaitValue()

        assertEquals(NetworkResult.Loading, actualUploadState)
    }

    @Test
    fun `when invalidate story then story is invalid`() {
        storyViewModel.invalidateStories()

        val actualIsStoryValid = storyViewModel.isStoriesInvalid.getOrAwaitValue()

        assertTrue(actualIsStoryValid)
    }

    @Test
    fun `when validate story then story is valid`() {
        storyViewModel.validateStories()

        val actualIsStoryValid = storyViewModel.isStoriesInvalid.getOrAwaitValue()

        assertFalse(actualIsStoryValid)
    }

    @Test
    fun `when upload image then error is false`() = mainCoroutineRule.runBlockingTest {
        val file = File("src/test/resources/Cover.png")

        `when`(userStoryRepository.postStory(file, "desc", 0f)).thenReturn(true)

        storyViewModel.uploadImage(file, "desc", 0f)

        val actualUploadState = storyViewModel.uploadStoryState.getOrAwaitValue()

        Mockito.verify(userStoryRepository).postStory(file, "desc", 0f)

        assertEquals(NetworkResult.Success(true), actualUploadState)
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

    @Test
    fun `when Get Paginated Story should not null`() = mainCoroutineRule.runBlockingTest {

        val dummyData = DataDummy.generateDummyStories(false)

        val data = PagingData.from(dummyData)

        val stories = MutableLiveData<PagingData<Story>>()

        stories.value = data

        `when`(userStoryRepository.getAllStories()).thenReturn(flowOf(data))

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryListAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            mainDispatcher = mainCoroutineRule.dispatcher,
            workerDispatcher = mainCoroutineRule.dispatcher,
        )

        mainCoroutineRule.launch {
            storyViewModel.getPaginatedStories().collectLatest {
                differ.submitData(it)
            }
        }

        advanceUntilIdle()

        Mockito.verify(userStoryRepository).getAllStories()

        assertNotNull(differ.snapshot())

        assertEquals(dummyData.size, differ.snapshot().size)

        assertEquals(dummyData[0].id, differ.snapshot()[0]?.id)
    }
}


val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}