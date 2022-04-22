package com.robertas.storyapp.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.robertas.storyapp.MainCoroutineRule
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.getOrAwaitValue
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.repositories.UserAccountRepository
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
import org.mockito.junit.MockitoJUnitRunner
import java.lang.RuntimeException

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class RegisterViewModelTest{

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var userAccountRepository: UserRepository

    private lateinit var registerViewModel: RegisterViewModel

    @Before
    fun setUp(){
        registerViewModel = RegisterViewModel(userAccountRepository)
    }

    @Test
    fun `when finish navigating then register state is Loading`(){
        registerViewModel.doneNavigating()

        val registerState = registerViewModel.registerState

        assertEquals(registerState.value, NetworkResult.Loading)
    }

    @Test
    fun `register user then return register state is true`() = mainCoroutineRule.runBlockingTest{
        `when`(userAccountRepository.register("agung", "agungs@gmail.com", "123456")).thenReturn(true)

        registerViewModel.register("agung", "agungs@gmail.com", "123456")

        val actualRegisterState = registerViewModel.registerState.getOrAwaitValue()

        Mockito.verify(userAccountRepository).register("agung", "agungs@gmail.com", "123456")

        assertEquals(actualRegisterState, NetworkResult.Success(true))
    }

    @Test
    fun `when register failed then register state is error`() = mainCoroutineRule.runBlockingTest {
        `when`(userAccountRepository.register("", "agungs@gmail.com", "123456")).thenThrow(RuntimeException("nama harus diisi"))

        registerViewModel.register("", "agungs@gmail.com", "123456")

        val actualRegisterState = registerViewModel.registerState.getOrAwaitValue()

        Mockito.verify(userAccountRepository).register("", "agungs@gmail.com", "123456")

        assertEquals(actualRegisterState, NetworkResult.Error("nama harus diisi"))
    }
}
