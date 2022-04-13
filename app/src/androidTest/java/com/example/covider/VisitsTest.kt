package com.example.covider

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.contrib.RecyclerViewActions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.hamcrest.Matchers
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Testing Map View
 */


@RunWith(AndroidJUnit4::class)
class VisitsTest {
    val delay: Long = 3000

    @Before
    fun login() {
        Firebase.auth.signInWithEmailAndPassword("existinguser@usc.edu", "password")
    }

    @Test
    fun testAddVisitButton() {
        launchActivity<MainActivity>().use { scenario ->
            // load visit fragment
            onView(withId(R.id.navigation_visits)).perform(click())
            Thread.sleep(delay)

            // click on add visit
            onView(withId(R.id.button_add_visit_page)).perform(click())
            Thread.sleep(1000)

            onView(withId(R.id.button_add_visit)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testVisitList() {
        launchActivity<MainActivity>().use { scenario ->
            // load visit fragment
            onView(withId(R.id.navigation_visits)).perform(click())
            Thread.sleep(delay)

            // look for entry
            onView(withId(R.id.visit_list)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText("TCC")))
            )
        }
    }
}