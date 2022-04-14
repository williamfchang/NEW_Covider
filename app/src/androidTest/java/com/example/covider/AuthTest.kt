package com.example.covider

import android.util.Log
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Testing Create Account and Login
 * https://developer.android.com/guide/components/activities/testing
 * https://medium.com/stepstone-tech/better-tests-with-androidxs-activityscenario-in-kotlin-part-1-6a6376b713ea
 *
 */


@RunWith(AndroidJUnit4::class)
class AuthTest {
    val transitionDelay: Long = 3000

    @Test
    fun testCreateAccountBadInput() {
        launchActivity<CreateAccountActivity>().use { scenario ->
            // check that create account button exists
            onView(withId(R.id.button_create)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
            onView(withId(R.id.button_create)).check(matches(withText("CREATE ACCOUNT")))

            // try clicking the create button
            onView(withId(R.id.button_create)).perform(click())
            onView(withId(R.id.button_create)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

            // now only fill in name
            onView(withId(R.id.input_name)).perform(typeText("New user"))

            onView(withId(R.id.button_create)).perform(click())
            onView(withId(R.id.button_create)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))

            // now fill in improper email
            onView(withId(R.id.input_usc_id)).perform(typeText("1234567890"))
            onView(withId(R.id.input_email)).perform(typeText("bad email"))
            onView(withId(R.id.input_password)).perform(typeText("password"))

            onView(withId(R.id.button_create)).perform(click())
            onView(withId(R.id.button_create)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        }
    }

    @Test
    fun testCreateAccountSuccess() {
        launchActivity<CreateAccountActivity>().use { scenario ->
            // fill out account creation
            onView(withId(R.id.input_name)).perform(typeText("New user"))
            onView(withId(R.id.input_usc_id)).perform(typeText("1234567890"))
            onView(withId(R.id.input_email)).perform(typeText("newuser@usc.edu"))
            onView(withId(R.id.input_password)).perform(typeText("password"))

            onView(withId(R.id.button_create)).perform(click())

            // check that we went to main activity
            Thread.sleep(transitionDelay)
            onView(withId(R.id.button_create)).check(doesNotExist())

//            // log out and check if on login page
//            onView(withId(R.id.navigation_settings)).perform(click())
//            onView(withId(R.id.button_logout)).perform(click())
//            onView(withId(R.id.button_create)).check(matches(isDisplayed()))
        }

        // Delete user so we can run these tests again successfully
        val user = Firebase.auth.currentUser!!

        user.delete().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG(), "User account deleted")
            }
            else {
                Log.d(TAG(), "Unable to delete user account")
            }
        }

        // Sign out user for future tests
        Firebase.auth.signOut()
    }

    @Test
    fun testClickCreateAccountFromLoginPage() {
        launchActivity<LoginActivity>().use { scenario ->
            // click login button
            onView(withId(R.id.button_login)).perform(click())

            // check for create account
            onView(withId(R.id.button_create)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun testLogin() {
        // Make sure we're logged out
        Firebase.auth.signOut()
        Thread.sleep(transitionDelay)

        // Now test login on page
        launchActivity<LoginActivity>().use { scenario ->
            Thread.sleep(transitionDelay)

            // Login with account
            onView(withId(R.id.input_email)).perform(typeText("existinguser@usc.edu"))
            onView(withId(R.id.input_password)).perform(typeText("password"))
            onView(withId(R.id.button_create)).perform(click())

            // Check if we log in successfully
            Thread.sleep(transitionDelay)
            onView(withId(R.id.button_create)).check(doesNotExist())
        }

        // Log out
        Firebase.auth.signOut()
    }
}