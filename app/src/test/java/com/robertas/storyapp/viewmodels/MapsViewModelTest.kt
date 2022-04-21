package com.robertas.storyapp.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.robertas.storyapp.DataDummy
import com.robertas.storyapp.MainCoroutineRule
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.getOrAwaitValue
import com.robertas.storyapp.models.enums.NetworkResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.times
import org.mockito.junit.MockitoJUnitRunner
import java.lang.RuntimeException

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MapsViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var userStoryRepository: StoryRepository

    private lateinit var mapsViewModel: MapsViewModel

    private val dummyStories = DataDummy.generateDummyStories(true)

    @Before
    fun setUp(){
        mapsViewModel = MapsViewModel(userStoryRepository)
    }

    @Test
    fun `when view model init then getAllStories`() = mainCoroutineRule.runBlockingTest{

        Mockito.verify(userStoryRepository, times(1)).getAllStories(true)
    }

    @Test
    fun `get all stories should return success with story`() = mainCoroutineRule.runBlockingTest{
        `when`(userStoryRepository.getAllStories(true)).thenReturn(dummyStories)

        mapsViewModel.getAllStories()

        val actualLoadStoryState = mapsViewModel.loadStoryState.getOrAwaitValue()

        assertEquals(NetworkResult.Success(dummyStories), actualLoadStoryState)

        assertNotEquals(NetworkResult.Success(null), actualLoadStoryState)

        Mockito.verify(userStoryRepository, times(2)).getAllStories(true)
    }

    @Test
    fun `when error loading story then result error`() = mainCoroutineRule.runBlockingTest {
        `when`(userStoryRepository.getAllStories(true)).thenThrow(RuntimeException("Missing Authentication"))

        mapsViewModel.getAllStories()

        val actualLoadStoryState = mapsViewModel.loadStoryState.getOrAwaitValue()

        assertEquals(NetworkResult.Error("Missing Authentication"), actualLoadStoryState)

        Mockito.verify(userStoryRepository, times(2)).getAllStories(true)
    }
}