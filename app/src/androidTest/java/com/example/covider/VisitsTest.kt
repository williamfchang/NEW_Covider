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
import com.example.covider.models.Visit
import com.example.covider.visits.AddVisitActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
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
        Thread.sleep(delay) // so user can log in

        // add fake entry
        val db = Firebase.firestore

        var startTime = Calendar.getInstance()
        startTime[Calendar.HOUR_OF_DAY] = 10
        startTime[Calendar.MINUTE] = 0
        val ts = Timestamp(startTime.time)

        var endTime = Calendar.getInstance()
        endTime[Calendar.HOUR_OF_DAY] = 11
        endTime[Calendar.MINUTE] = 30
        val te = Timestamp(endTime.time)

        val userID = Firebase.auth.currentUser!!.uid
        val section = "1"

        val tempVisit = Visit(ts, te, "SAL", userID, false, section)
        db.collection("visits").document("tempVisit").set(tempVisit)

        launchActivity<MainActivity>().use { scenario ->
            Thread.sleep(delay)

            // load visit fragment
            onView(withId(R.id.navigation_visits)).perform(click())
            Thread.sleep(delay)

            // look for temp entry
            onView(withId(R.id.visit_list)).perform(
                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
                hasDescendant(withText("SAL")))
            )
        }

        // delete entry
        db.collection("visits").document("tempVisit").delete()
        Thread.sleep(1000)
    }

    @Test
    fun testAddVisitAutofill() {
        // Check if adding visit from the building info page will autopopulate the building dropdown
        launchActivity<BuildingsActivity>().use { scenario ->
            Thread.sleep(delay)

            // Click on first building (LVL)
            Espresso.onData(Matchers.anything()).inAdapterView(withId(R.id.building_list_view))
                .atPosition(0).perform(click())

            onView(withId(R.id.building_id_label)).check(matches(withText("LVL")))

            // Click on add visit from info page
            onView(withId(R.id.button_add_building_visit)).perform(click())
            Thread.sleep(delay)

            // Check if dropdown is listed as LVL
            onView(withId(R.id.spinner_building)).check(matches(withSpinnerText("LVL")))
        }
    }

    @Test
    fun testAddVisit() {
        launchActivity<MainActivity>().use { scenario ->
            Thread.sleep(delay)

            // navigate to add visit page
            onView(withId(R.id.navigation_visits)).perform(click())
            Thread.sleep(delay)
            onView(withId(R.id.button_add_visit_page)).perform(click())
            Thread.sleep(1000)

            // choose building ACC
            onView(withId(R.id.spinner_building)).perform(click())
            onData(anything()).atPosition(1).perform(click())

            // allow default time

            // add visit
            onView(withId(R.id.button_add_visit)).perform(click())

            Thread.sleep(delay)

            // Now check if visit shows
            onView(withId(R.id.navigation_visits)).perform(click())
            Thread.sleep(1000)

            onView(withId(R.id.visit_list)).check(matches(isDisplayed()))
//            onView(withId(R.id.visit_list)).perform(
//                RecyclerViewActions.scrollTo<RecyclerView.ViewHolder>(
//                    hasDescendant(withText("ACC")))
//            )

        }

        // Need to delete visit
        val query = Firebase.firestore.collection("visits")
            .whereEqualTo("userID", "enJovZOKLRYMGpcldIggvv47zkB2")
            .whereEqualTo("buildingID", "ACC")

        query.get().addOnSuccessListener { docs ->
            Log.i(TAG(), "deleting ${docs.count()} entries")
            for (d in docs) {
                d.reference.delete()
            }
        }
    }

    @Test
    fun testRecurringTextBoxes() {
        launchActivity<MainActivity>().use { scenario ->
            Thread.sleep(delay)

            // navigate to add visit page
            onView(withId(R.id.navigation_visits)).perform(click())
            Thread.sleep(delay)
            onView(withId(R.id.button_add_visit_page)).perform(click())
            Thread.sleep(1000)

            // class code and section id text boxes should be hidden
            onView(withId(R.id.input_course_code)).check(matches(not(isDisplayed())))
            onView(withId(R.id.input_course_section)).check(matches(not(isDisplayed())))

            // click switch for recurring
            onView(withId(R.id.switch_class)).perform(click())

            // check class code nad section id again
            onView(withId(R.id.input_course_code)).check(matches(isDisplayed()))
            onView(withId(R.id.input_course_section)).check(matches(isDisplayed()))
        }
    }
}