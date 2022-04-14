package com.example.covider

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.launchActivity
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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Testing Map View
 */



@RunWith(AndroidJUnit4::class)
class MapAndBuildingListTest {
    val delay: Long = 3000

    @Before
    fun login() {
        Firebase.auth.signInWithEmailAndPassword("existinguser@usc.edu", "password")
//        val db = Firebase.firestore
//        val name = db.collection("buildings").document("ABA").id
//        Log.i(TAG(), "ABCDE $name")
    }

    @Test
    fun testButtons() {
        launchActivity<MainActivity>().use { scenario ->
            // Check that we're on map page
            onView(withId(R.id.map)).check(matches(isDisplayed()))

            // Go to building list page
            onView(withId(R.id.button_building_list)).perform(click())
            onView(withId(R.id.building_list_view)).check(matches(isDisplayed()))
        }
    }


    @Test(expected = PerformException::class)
    fun lookForNonexistentBuilding() {
        launchActivity<BuildingsActivity>().use { scenario ->
            onView(withId(R.id.building_list_view)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText("fake building"))))
        }
    }

    @Test
    fun clickOnBuildingEntry() {
        launchActivity<BuildingsActivity>().use { scenario ->
            Thread.sleep(delay)

            onData(anything()).inAdapterView(withId(R.id.building_list_view))
                .atPosition(0).perform(click())

            // Check if we went to building details page
            onView(withId(R.id.building_name_label)).check(matches(isDisplayed()))

            Thread.sleep(1000)
        }
    }

    @Test
    fun clickOnAddVisitInBuildingInfo() {
        launchActivity<BuildingsActivity>().use { scenario ->
            Thread.sleep(delay)

            onData(anything()).inAdapterView(withId(R.id.building_list_view))
                .atPosition(0).perform(click())

            // Check if we went to building details page
            onView(withId(R.id.building_name_label)).check(matches(isDisplayed()))
            onView(withId(R.id.building_id_label)).check(matches(withText("LVL")))

            Thread.sleep(1000)

            // Now click on add visits
            onView(withId(R.id.button_add_building_visit)).perform(click())
            onView(withId(R.id.button_add_visit)).check(matches(isDisplayed()))
        }
    }
}