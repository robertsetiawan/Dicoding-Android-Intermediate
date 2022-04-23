package com.robertas.storyapp.viewmodels

import com.robertas.storyapp.CoroutinesTestRule
import com.robertas.storyapp.DataDummy
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.models.enums.NetworkResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
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


@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class UserViewModelTest {

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    @Mock
    private lateinit var userAccountRepository: UserRepository

    private lateinit var userViewModel: UserViewModel

    private val dummyUser = DataDummy.generateUserDummy()

    @Before
    fun setUp() {
        userViewModel = UserViewModel(userAccountRepository)
    }


    @Test
    fun `when success login then return true`() = runTest {

//        `when`(userAccountRepository.isUserLoggedIn()).thenReturn(true)

        `when`(userAccountRepository.login(dummyUser.name, "123456")).thenReturn(flowOf(NetworkResult.Success(dummyUser)))

        userViewModel.login(dummyUser.name, "123456").collect { result ->

            assertTrue(result is NetworkResult.Success)

            assertFalse(result is NetworkResult.Error)

            assertNotNull((result as NetworkResult.Success).data)

            assertEquals(dummyUser.userId, result.data!!.userId)

            Mockito.verify(userAccountRepository).login(dummyUser.name, "123456")
        }
    }



    @Test
    fun `when error login then return false`(): Unit = runTest {

//        `when`(userAccountRepository.isUserLoggedIn()).thenReturn(false)

        val expectedResult = flowOf(NetworkResult.Error("password salah"))

        `when`(userAccountRepository.login(dummyUser.name, "123456")).thenReturn(expectedResult)

        userViewModel.login(dummyUser.name, "123456").collect { result ->

            assertFalse(result is NetworkResult.Success)
            assertTrue(result is NetworkResult.Error)

            assertEquals("password salah", (result as NetworkResult.Error).message)

            Mockito.verify(userAccountRepository).login(dummyUser.name, "123456")
        }
    }


    @Test
    fun `when user logout then return false`() {

        `when`(userAccountRepository.logOut()).then {

            `when`(userAccountRepository.isUserLoggedIn()).thenReturn(false)

        }
        userViewModel.logOut()

        val actualIsLogin = userViewModel.isUserLoggedIn()

        Mockito.verify(userAccountRepository).logOut()

        assertFalse(actualIsLogin)
    }


    @Test
    fun `get language mode will result saved language mode`(){

        val expectedLanguage = "id"

        userViewModel.setLanguageMode(expectedLanguage)

        `when`(userAccountRepository.getLanguageMode()).thenReturn(expectedLanguage)

        val actualLanguage = userViewModel.getLanguageMode()

        assertEquals(expectedLanguage, actualLanguage)

        Mockito.verify(userAccountRepository).setLanguageMode(expectedLanguage)

        Mockito.verify(userAccountRepository).getLanguageMode()
    }


    @Test
    fun `register user then return register state is true`() = runTest{
        `when`(userAccountRepository.register("agung", "agungs@gmail.com", "123456")).thenReturn(
            flowOf(NetworkResult.Success(true)))

        userViewModel.register("agung", "agungs@gmail.com", "123456").collect { result ->

            assert(result is NetworkResult.Success)

            assertTrue((result as NetworkResult.Success).data)

            Mockito.verify(userAccountRepository).register("agung", "agungs@gmail.com", "123456")
        }
    }

    @Test
    fun `when register failed then register state is error`() = runTest {
        `when`(userAccountRepository.register("agung", "agungs@gmail.com", "123456")).thenReturn(
            flowOf(NetworkResult.Error("nama kurang panjang")))

        userViewModel.register("agung", "agungs@gmail.com", "123456").collect { result ->

            assert(result is NetworkResult.Error)

            assertEquals("nama kurang panjang", (result as NetworkResult.Error).message)

            Mockito.verify(userAccountRepository).register("agung", "agungs@gmail.com", "123456")
        }
    }
}