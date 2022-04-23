package com.robertas.storyapp.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.robertas.storyapp.CoroutinesTestRule
import com.robertas.storyapp.DataDummy
import com.robertas.storyapp.MainCoroutineRule
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.models.enums.NetworkResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MapsViewModelTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var userStoryRepository: StoryRepository

    @Mock
    private lateinit var userAccountRepository: UserRepository

    private lateinit var mapsViewModel: MapsViewModel

    private val dummyStories = DataDummy.generateDummyStories(true)

    private val dummyToken = DataDummy.generateDummyToken()

    @Before
    fun setUp(){
        mapsViewModel = MapsViewModel(userAccountRepository, userStoryRepository)
    }

    @Test
    fun `get all stories should return success with story`() = runTest{

        val expectedPagingData = flowOf(NetworkResult.Success(dummyStories))

        `when`(userAccountRepository.getBearerToken()).thenReturn(dummyToken)

        `when`(userStoryRepository.getAllStories(dummyToken, true)).thenReturn(expectedPagingData)

        assertEquals(expectedPagingData, mapsViewModel.getListStory())

        Mockito.verify(userStoryRepository).getAllStories(dummyToken, true)

        Mockito.verify(userAccountRepository).getBearerToken()
    }
}