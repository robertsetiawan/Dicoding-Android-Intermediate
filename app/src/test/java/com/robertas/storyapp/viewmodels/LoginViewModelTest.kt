package com.robertas.storyapp.viewmodels

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.robertas.storyapp.DataDummy
import com.robertas.storyapp.MainCoroutineRule
import com.robertas.storyapp.abstractions.UserRepository
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
import org.mockito.junit.MockitoJUnitRunner


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

        val actualLoginState = loginViewModel.loginState

        Mockito.verify(userAccountRepository).login(dummyUser.name, "123456")

        assertEquals(actualLoginState.value, NetworkResult.Success(dummyUser))

        assertTrue(actualIsLogin)
    }

    @Test
    fun `when user logout then return false`() {
        `when`(userAccountRepository.isUserLoggedIn()).thenReturn(false)

        userAccountRepository.logOut()

        val actualIsLogin = loginViewModel.isUserLoggedIn()

        Mockito.verify(userAccountRepository).logOut()

        assertFalse(actualIsLogin)
    }

    @Test
    fun `when finish navigating then LoginState is loading`() {
        loginViewModel.doneNavigating()

        val loginState = loginViewModel.loginState

        assertEquals(loginState.value, NetworkResult.Loading)
    }

    @Test
    fun `get language will result settled language`(){

        val expectedLanguage = "id"

        loginViewModel.setLanguageMode(expectedLanguage)

        `when`(userAccountRepository.getLanguageMode()).thenReturn(expectedLanguage)

        val actualLanguage = userAccountRepository.getLanguageMode()

        assertEquals(actualLanguage, expectedLanguage)

        Mockito.verify(userAccountRepository).setLanguageMode(expectedLanguage)

        Mockito.verify(userAccountRepository).getLanguageMode()
    }

}