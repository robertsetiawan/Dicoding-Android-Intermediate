package com.robertas.storyapp.repositories

import android.content.SharedPreferences
import android.provider.ContactsContract
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.robertas.storyapp.CoroutinesTestRule
import com.robertas.storyapp.DataDummy
import com.robertas.storyapp.MainCoroutineRule
import com.robertas.storyapp.abstractions.IDomainMapper
import com.robertas.storyapp.abstractions.IStoryService
import com.robertas.storyapp.models.domain.User
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.models.network.UserNetwork
import com.robertas.storyapp.utils.UserNetworkMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
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

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class UserAccountRepositoryTest {

    @Mock
    private lateinit var pref: SharedPreferences

    @get:Rule
    var coroutinesTestRule = CoroutinesTestRule()

    private lateinit var userAccountRepository: UserAccountRepository

    @Mock
    private lateinit var apiService: IStoryService

    private lateinit var networkMapper: IDomainMapper<UserNetwork, User>

    private val dummySuccessUserResponse = DataDummy.generateUserResponseDummy(false)

    @Before
    fun setUp() {
        networkMapper = UserNetworkMapper()

        userAccountRepository = UserAccountRepository(apiService, pref, networkMapper)
    }


    @Test
    fun `when success login then return success`() = runTest {

        `when`(apiService.postLogin("email@gmail.com", "123456")).thenReturn(
            dummySuccessUserResponse)

        val flow = userAccountRepository.login("email@gmail.com", "123456").toList()

        assert(NetworkResult.Success(DataDummy.generateUserDummy()) in flow)
    }


    @Test
    fun `when error login then return error`() = runTest {

        `when`(apiService.postLogin("email@gmail.com", "123456")).thenReturn(
            DataDummy.generateUserResponseDummy(true)
        )

        val flow = userAccountRepository.login("email@gmail.com", "123456").toList()

        assert(NetworkResult.Error(DataDummy.generateUserResponseDummy(true).message) in flow)
    }

    @Test
    fun `when user register then return true`() = runTest {
        `when`(apiService.register("agung", "agung@gmail.com", "123456")).thenReturn(
            DataDummy.generateUserResponseDummy(false)
        )

        val flow = userAccountRepository.register("agung", "agung@gmail.com", "123456").toList()

        assert(NetworkResult.Success(true) in flow)
    }


    @Test
    fun `when error user register then return false`() = runTest {
        `when`(apiService.register("agung", "agung@gmail.com", "123456")).thenReturn(
            DataDummy.generateUserResponseDummy(true)
        )

        val flow = userAccountRepository.register("agung", "agung@gmail.com", "123456").toList()

        assert(NetworkResult.Error("error is true") in flow)
    }

    @Test
    fun `get user token from shared preference`(){
        `when`(pref.getString(USER_TOKEN_KEY, null)).thenReturn("1234567")

        val actualToken =userAccountRepository.getUserToken()

        assertEquals("1234567", actualToken)
    }

    companion object {
        const val USER_TOKEN_KEY = "user_token_key"
    }
}