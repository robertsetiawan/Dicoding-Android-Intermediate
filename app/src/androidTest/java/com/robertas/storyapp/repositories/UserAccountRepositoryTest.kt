package com.robertas.storyapp.repositories

import android.content.Context
import android.content.SharedPreferences
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.robertas.storyapp.DataDummy
import com.robertas.storyapp.FakeStoryService
import com.robertas.storyapp.StoryApp
import com.robertas.storyapp.abstractions.IDomainMapper
import com.robertas.storyapp.abstractions.IStoryService
import com.robertas.storyapp.abstractions.UserRepository
import com.robertas.storyapp.models.domain.User
import com.robertas.storyapp.models.enums.CameraMode
import com.robertas.storyapp.models.enums.LanguageMode
import com.robertas.storyapp.models.network.UserNetwork
import com.robertas.storyapp.utils.UserNetworkMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class UserAccountRepositoryTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var storyService: IStoryService

    private lateinit var userAccountRepository: UserRepository

    private lateinit var networkMapper: IDomainMapper<UserNetwork, User>

    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setUp() {
        val context = getApplicationContext<StoryApp>()

        sharedPreferences = context.getSharedPreferences(
            "user_setting_preferences",
            Context.MODE_PRIVATE
        )

        storyService = FakeStoryService()

        networkMapper = UserNetworkMapper()

        userAccountRepository =
            UserAccountRepository(storyService, sharedPreferences, networkMapper)
    }

    @After
    fun tearDown(){
        sharedPreferences.edit().clear().apply()
    }

    @Test
    fun whenLoginThenReturnUser() = runBlockingTest {

        val email = "email@gmail.com"

        val password = "123456"

        val job = launch {
            val data = userAccountRepository.login(email, password)

            Assert.assertNotNull(data)

            Assert.assertEquals(DataDummy.generateUserDummy().name, data!!.name)

            Assert.assertEquals(DataDummy.generateUserDummy().userId, data.userId)

            Assert.assertEquals(DataDummy.generateUserDummy().token, data.token)
        }

        job.cancel()
    }

    @Test
    fun whenRegisterUserThenReturnTrue() = runBlockingTest {

        val email = "email@gmail.com"

        val password = "123456"

        val name = "budi"



        val job = launch {
            val error: Boolean = userAccountRepository.register(name, email, password)

            Assert.assertFalse(error)
        }

        job.cancel()
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