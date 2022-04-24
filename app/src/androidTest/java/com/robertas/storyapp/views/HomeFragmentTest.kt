package com.robertas.storyapp.views

import androidx.navigation.testing.TestNavHostController
import androidx.paging.ExperimentalPagingApi
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.MediumTest
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import com.robertas.storyapp.JsonConverter
import com.robertas.storyapp.R
import com.robertas.storyapp.launchFragmentInHiltContainer
import com.robertas.storyapp.modules.NetworkModule
import com.robertas.storyapp.utils.EspressoIdlingResource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalPagingApi
@MediumTest
@HiltAndroidTest
class HomeFragmentTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    private val mockWebServer = MockWebServer()

    @Before
    fun setUp() {
        mockWebServer.start(8080)

        NetworkModule.API_URL = "http://127.0.0.1:8080/"

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()

        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }


    @Test
    fun launchHomeFragment_Error() {

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        runOnUiThread {

            navController.setGraph(R.navigation.nav_graph)

            navController.setCurrentDestination(R.id.homeFragment)
        }

        launchFragmentInHiltContainer<HomeFragment>(null, R.style.Theme_StoryApp, navController)

        val mockResponse = MockResponse()
            .setResponseCode(500)

        mockWebServer.enqueue(mockResponse)

        onView(withId(R.id.empty_layout))
            .check(matches(isDisplayed()))

        onView(withId(R.id.no_data_tv))
            .check(matches(isDisplayed()))
    }


    @Test
    fun launchHomeFragment_Empty() {

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        runOnUiThread {

            navController.setGraph(R.navigation.nav_graph)

            navController.setCurrentDestination(R.id.homeFragment)
        }

        launchFragmentInHiltContainer<HomeFragment>(null, R.style.Theme_StoryApp, navController)

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(JsonConverter.readStringFromFile("empty_response.json"))

        mockWebServer.enqueue(mockResponse)

        onView(withId(R.id.empty_layout))
            .check(matches(isDisplayed()))

        onView(withId(R.id.no_data_tv))
            .check(matches(isDisplayed()))
    }

    @Test
    fun launchHomeFragment_Success() {

        val navController = TestNavHostController(ApplicationProvider.getApplicationContext())

        runOnUiThread {

            navController.setGraph(R.navigation.nav_graph)

            navController.setCurrentDestination(R.id.homeFragment)
        }

        launchFragmentInHiltContainer<HomeFragment>(null, R.style.Theme_StoryApp, navController)

        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody(JsonConverter.readStringFromFile("response.json"))

        mockWebServer.enqueue(mockResponse)

        onView(withId(R.id.story_list))
            .check(matches(isDisplayed()))

        onView(withText("reviewer33"))
            .check(matches(isDisplayed()))
    }
}