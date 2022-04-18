package com.robertas.storyapp.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.robertas.storyapp.MainCoroutineRule
import com.robertas.storyapp.abstractions.StoryRepository
import com.robertas.storyapp.abstractions.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

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
    fun setUp(){
        storyViewModel = StoryViewModel(userStoryRepository, userAccountRepository)
    }

    @Test
    fun `when finish navigating then upload state is loading`(){

    }
}