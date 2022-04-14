package com.example.covider

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.PerformException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.contrib.RecyclerViewActions
import com.example.covider.models.CoviderEnums
import com.example.covider.models.User
import com.example.covider.models.Visit
import com.example.covider.visits.AddVisitActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*


/**
 * Testing Map View
 */


@RunWith(AndroidJUnit4::class)
class ProfileTest {
    val delay: Long = 3000

    @Before
    fun login() {
        Firebase.auth.signInWithEmailAndPassword("existinguser@usc.edu", "password")
    }

    @Test
    fun checkUserInfo() {
        val user = User("Test user", "existinguser@usc.edu", "6789012345",
            CoviderEnums.UserType.STUDENT)

        // now start the app
        launchActivity<MainActivity>().use { scenario ->
            // navigate to add profile page
            Thread.sleep(delay)
            onView(withId(R.id.navigation_settings)).perform(click())
            Thread.sleep(delay)

            // check name
            onView(withId(R.id.text_name)).check(matches(withText(user.name)))

            // check student vs instructor
            onView(withId(R.id.user_type)).check(matches(withText("STUDENT")))

            // check email and ID
            onView(withId(R.id.view_email)).check(matches(withText(user.email)))
            onView(withId(R.id.view_id)).check(matches(withText(user.uscID)))
        }
    }

    @Test
    fun testUploadHealthReportButton() {
        launchActivity<MainActivity>().use { scenario ->
            // navigate to add profile page
            Thread.sleep(delay)
            onView(withId(R.id.navigation_settings)).perform(click())
            Thread.sleep(delay)

            // try upload health report button
            onView(withId(R.id.button_upload_health_reports)).perform(click())
            Thread.sleep(delay)
            onView(withId(R.id.symptomsTextView)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testLogout() {
        launchActivity<MainActivity>().use { scenario ->
            // navigate to add profile page
            Thread.sleep(delay)
            onView(withId(R.id.navigation_settings)).perform(click())
            Thread.sleep(delay)

            // try logout button
            onView(withId(R.id.button_logout)).perform(click())
            Thread.sleep(delay)

            // check if we're back on login page
            onView(withId(R.id.button_create)).check(matches(isDisplayed()))
        }
    }
}