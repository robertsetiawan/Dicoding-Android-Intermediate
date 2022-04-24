package com.robertas.storyapp.views

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.robertas.storyapp.R
import com.robertas.storyapp.utils.EspressoIdlingResource
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@LargeTest
@HiltAndroidTest
class MainActivityTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @get:Rule
    val activity = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setUp() {

        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)

        Intents.init()
    }

    @After
    fun teardown() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)

        Intents.release()
    }

    @Test
    fun loadStoryAndPostStory(){

        onView(withId(R.id.story_list))
            .check(matches(isDisplayed()))

        onView(withId(R.id.floating_btn)).perform(
            click()
        )

        onView(withId(R.id.camera_btn)).perform(
            click()
        )

        onView(withId(R.id.capture_btn)).perform(
            click()
        )

        onView(withId(R.id.desc_portrait_et)).perform(
            click(),
            typeText("hehe"),
            closeSoftKeyboard()
        )

        onView(withId(R.id.upload_btn)).perform(
            click()
        )
    }
}