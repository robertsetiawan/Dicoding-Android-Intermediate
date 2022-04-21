package com.robertas.storyapp.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.robertas.storyapp.DataDummy
import com.robertas.storyapp.MainCoroutineRule
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.getOrAwaitValue
import com.robertas.storyapp.models.enums.NetworkResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.lang.Exception
import java.lang.RuntimeException


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LoginViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var userAccountRepository: UserRepository

    private lateinit var loginViewModel: LoginViewModel

    private val dummyUser = DataDummy.generateUserDummy()

    @Before
    fun setUp() {
        loginViewModel = LoginViewModel(userAccountRepository)
    }

    @Test
    fun `when user login then return true`() = mainCoroutineRule.runBlockingTest {

        `when`(userAccountRepository.isUserLoggedIn()).thenReturn(true)

        `when`(userAccountRepository.login(dummyUser.name, "123456")).thenReturn(dummyUser)

        loginViewModel.login(dummyUser.name, "123456")

        val actualIsLogin = loginViewModel.isUserLoggedIn()

        val actualLoginState = loginViewModel.loginState.getOrAwaitValue()

        Mockito.verify(userAccountRepository).login(dummyUser.name, "123456")

        assertEquals(NetworkResult.Success(dummyUser), actualLoginState)

        assertNotEquals(NetworkResult.Success(null), actualLoginState)

        assertTrue(actualIsLogin)
    }


    @Test
    fun `when user logout then return false`() {

        `when`(userAccountRepository.logOut()).then {

            `when`(userAccountRepository.isUserLoggedIn()).thenReturn(false)

        }
        loginViewModel.logOut()

        val actualIsLogin = loginViewModel.isUserLoggedIn()

        Mockito.verify(userAccountRepository).logOut()

        assertFalse(actualIsLogin)
    }


    @Test
    fun `when login error then result is error`() = mainCoroutineRule.runBlockingTest {

        `when`(userAccountRepository.login(dummyUser.name, "123456")).thenThrow(RuntimeException("password harus lebih dari 6"))

        loginViewModel.login(dummyUser.name, "123456")

        val actualLoginState = loginViewModel.loginState.getOrAwaitValue()

        assertEquals(NetworkResult.Error("password harus lebih dari 6"), actualLoginState)

        Mockito.verify(userAccountRepository).login(dummyUser.name, "123456")
    }

    @Test
    fun `when finish navigating then LoginState is loading`() {
        loginViewModel.doneNavigating()

        val loginState = loginViewModel.loginState.getOrAwaitValue()

        assertEquals(NetworkResult.Loading, loginState)
    }

    @Test
    fun `get language mode will result saved language mode`(){

        val expectedLanguage = "id"

        loginViewModel.setLanguageMode(expectedLanguage)

        `when`(userAccountRepository.getLanguageMode()).thenReturn(expectedLanguage)

        val actualLanguage = userAccountRepository.getLanguageMode()

        assertEquals(expectedLanguage, actualLanguage)

        Mockito.verify(userAccountRepository).setLanguageMode(expectedLanguage)

        Mockito.verify(userAccountRepository).getLanguageMode()
    }

}