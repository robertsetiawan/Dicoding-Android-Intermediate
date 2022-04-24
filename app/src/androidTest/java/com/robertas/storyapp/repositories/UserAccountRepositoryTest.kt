package com.robertas.storyapp.repositories

import android.content.SharedPreferences
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.robertas.storyapp.DataDummy
import com.robertas.storyapp.FakeStoryService
import com.robertas.storyapp.abstractions.IDomainMapper
import com.robertas.storyapp.abstractions.IStoryService
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.models.domain.User
import com.robertas.storyapp.models.enums.CameraMode
import com.robertas.storyapp.models.enums.LanguageMode
import com.robertas.storyapp.models.enums.NetworkResult
import com.robertas.storyapp.models.network.UserNetwork
import com.robertas.storyapp.utils.UserNetworkMapper
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith
import javax.inject.Inject

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
@HiltAndroidTest
class UserAccountRepositoryTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private lateinit var storyService: IStoryService

    private lateinit var userAccountRepository: UserRepository

    private lateinit var networkMapper: IDomainMapper<UserNetwork, User>

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {

        hiltRule.inject()

        storyService = FakeStoryService(false)

        networkMapper = UserNetworkMapper()

        userAccountRepository =
            UserAccountRepository(storyService, sharedPreferences, networkMapper)
    }

    @After
    fun tearDown(){
        sharedPreferences.edit().clear().apply()
    }

    @Test
    fun whenSuccessLoginThenReturnUser() = runTest {

        val email = "email@gmail.com"

        val password = "123456"

        val list = userAccountRepository.login(email, password).toList()

        Assert.assertEquals(list, listOf(NetworkResult.Loading, NetworkResult.Success(DataDummy.generateUserDummy())))
    }


    @Test
    fun whenErrorLoginThenReturnError() = runTest {

        storyService = FakeStoryService(true)

        userAccountRepository =
            UserAccountRepository(storyService, sharedPreferences, networkMapper)

        val email = "email@gmail.com"

        val password = "123456"

        val list = userAccountRepository.login(email, password).toList()

        Assert.assertEquals(list, listOf(NetworkResult.Loading, NetworkResult.Error("error is true")))
    }

    @Test
    fun whenRegisterUserThenReturnTrue() = runTest {

        val email = "email@gmail.com"

        val password = "123456"

        val name = "budi"

        val list = userAccountRepository.register(name, email, password).toList()

        Assert.assertEquals(list, listOf(NetworkResult.Loading, NetworkResult.Success(true)))
    }

    @Test
    fun whenErrorRegisterUserThenReturnFalse() = runTest {

        storyService = FakeStoryService(true)

        userAccountRepository =
            UserAccountRepository(storyService, sharedPreferences, networkMapper)

        val email = "email@gmail.com"

        val password = "123456"

        val name = "budi"

        val list = userAccountRepository.register(name, email, password).toList()

        Assert.assertEquals(list, listOf(NetworkResult.Loading, NetworkResult.Error("error")))
    }

    @Test
    fun getUserReturnSavedUser(){
        userAccountRepository.setLoggedInUser(DataDummy.generateUserDummy())

        val actualIsLoggedIn = userAccountRepository.isUserLoggedIn()

        Assert.assertTrue(actualIsLoggedIn)
    }

    @Test
    fun logOutThenIsLoggedInIsFalse(){
        userAccountRepository.logOut()

        val actualIsLoggedIn = userAccountRepository.isUserLoggedIn()

        Assert.assertFalse(actualIsLoggedIn)
    }

    @Test
    fun getCameraModeReturnSavedCameraMode(){
        val expectedCameraMode = CameraMode.CAMERA_X

        userAccountRepository.setCameraMode(expectedCameraMode)

        val actualCameraMode = userAccountRepository.getCameraMode()

        Assert.assertEquals(expectedCameraMode, actualCameraMode)
    }

    @Test
    fun getLanguageModeReturnSavedLanguageMode() {
        val expectedLanguageMode = LanguageMode.EN

        userAccountRepository.setLanguageMode(expectedLanguageMode)

        val actualLanguageMode = userAccountRepository.getLanguageMode()

        Assert.assertEquals(expectedLanguageMode, actualLanguageMode)
    }
}