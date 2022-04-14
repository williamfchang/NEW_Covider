package com.example.covider

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.covider.models.Building
import com.example.covider.models.Visit
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.awaitility.Awaitility
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*
import java.util.concurrent.Callable
import kotlin.collections.HashMap

/**
 * https://stackoverflow.com/questions/52030607/testing-private-methods-in-kotlin
 *
 */

@RunWith(AndroidJUnit4::class)
class GeneralUnitTest {

    lateinit var db: FirebaseFirestore

    @Before
    fun initFirebase() {
        db = Firebase.firestore
    }

    @Test
    fun testBuildingSorting() {
        // set buildings variable
        val buildings = hashMapOf(
            "TCC" to Building(id = "TCC", priority = 2),
            "LVL" to Building(id = "LVL", priority = 1),
            "SAL" to Building(id = "SAL", priority = 0),
            "SGM" to Building(id = "SGM", priority = 2)
        )

        // invoke the sorting function we're testing
        val out = BuildingsActivity.sortBuildings(buildings)

        // check that priorities are correct
        var lowest = 2 // starts at 2

        for (title in out) {
            val buildingID = title.slice(0..2)
            val priority = buildings[buildingID]!!.priority

            // assertion
            Assert.assertTrue(priority <= lowest)

            // check if priority got lower (update lower)
            if (priority < lowest) {
                lowest = priority
            }
        }
    }

    @Test
    fun testBuildingSortingEmpty() {
        val out = BuildingsActivity.sortBuildings(hashMapOf())
        Assert.assertEquals(out.count(), 0)
    }

    @Test
    fun testBuildingSortingRandom() {
        // create buildings list, random
        var buildings = HashMap<String, Building>()

        for (i in 0..200) {
            val id = i.toString().padStart(3, '0')
            val currBuilding = Building(id, priority = (0..3).random())
            buildings[id] = currBuilding
        }

        // now sort
        val out = BuildingsActivity.sortBuildings(buildings)

        // check that priorities are correct
        var lowest = 2 // starts at 2

        for (title in out) {
            val buildingID = title.slice(0..2)
            val priority = buildings[buildingID]!!.priority

            // assertion
            Assert.assertTrue(priority <= lowest)

            // check if priority got lower (update lower)
            if (priority < lowest) {
                lowest = priority
            }
        }
    }


    var contactVisits: ArrayList<Visit>? = null

    fun contactTracingCallback(cV: ArrayList<Visit>) {
        contactVisits = cV
    }

    fun checkNullityOfContactVisits(): Callable<Boolean> {
        return Callable<Boolean> { contactVisits != null }
    }

    @Test
    fun testContactTracingSimple() {
        // Add a positive visit and a contact visit to Firebase
        var startTime = Calendar.getInstance()
        startTime[Calendar.HOUR_OF_DAY] = 10
        startTime[Calendar.MINUTE] = 0

        var endTime = Calendar.getInstance()
        endTime[Calendar.HOUR_OF_DAY] = 11
        endTime[Calendar.MINUTE] = 30

        db.collection("visits").document("visit1").set(
            Visit(Timestamp(startTime.time), Timestamp(endTime.time),
                "TCC", "user1", true, "1")
        )

        startTime[Calendar.HOUR_OF_DAY] = 11
        startTime[Calendar.MINUTE] = 0

        endTime[Calendar.HOUR_OF_DAY] = 12
        endTime[Calendar.MINUTE] = 10

        db.collection("visits").document("visit2").set(
            Visit(Timestamp(startTime.time), Timestamp(endTime.time),
                "TCC", "user2", false, "1")
        )

        Thread.sleep(1000)

        // run the contact tracing finding function
        ProfileFragment.queryForContact(db.collection("visits"),
            hashSetOf("TCC"), "user2", ::contactTracingCallback)

        Awaitility.await().until(checkNullityOfContactVisits())

        // make sure we have at least one contact
        Assert.assertTrue(contactVisits!!.count() > 0)

        // look for the contact we added
        var found = false
        for (visit in contactVisits!!) {
            if (visit.userID == "user1") {
                found = true
                break
            }
        }

        // Assert if we found it
        Assert.assertTrue(found)


        // clean up (delete the visits we just made)
        db.collection("visits").document("visit1").delete()
        db.collection("visits").document("visit2").delete()
    }


    @Test
    fun testContactTracingNonexactTime() {
        // Add a positive visit and a contact visit to Firebase
        var startTime = Calendar.getInstance()
        startTime[Calendar.HOUR_OF_DAY] = 9
        startTime[Calendar.MINUTE] = 0

        var endTime = Calendar.getInstance()
        endTime[Calendar.HOUR_OF_DAY] = 10
        endTime[Calendar.MINUTE] = 30

        db.collection("visits").document("visit1").set(
            Visit(Timestamp(startTime.time), Timestamp(endTime.time),
                "TCC", "user1", true, "1")
        )

        startTime[Calendar.HOUR_OF_DAY] = 11
        startTime[Calendar.MINUTE] = 0

        endTime[Calendar.HOUR_OF_DAY] = 12
        endTime[Calendar.MINUTE] = 10

        db.collection("visits").document("visit2").set(
            Visit(Timestamp(startTime.time), Timestamp(endTime.time),
                "TCC", "user2", false, "1")
        )

        Thread.sleep(1000)

        // run the contact tracing finding function
        ProfileFragment.queryForContact(db.collection("visits"),
            hashSetOf("TCC"), "user2", ::contactTracingCallback)

        Awaitility.await().until(checkNullityOfContactVisits())

        // make sure we have at least one contact
        Assert.assertTrue(contactVisits!!.count() > 0)

        // look for the contact we added
        var found = false
        for (visit in contactVisits!!) {
            if (visit.userID == "user1") {
                found = true
                break
            }
        }

        // Assert if we found it
        Assert.assertTrue(found)


        // clean up (delete the visits we just made)
        db.collection("visits").document("visit1").delete()
        db.collection("visits").document("visit2").delete()
    }

    @Test
    fun testContactTracingNoContact() {
        // Add a positive visit and a contact visit to Firebase
        var startTime = Calendar.getInstance()
        startTime.add(Calendar.DAY_OF_YEAR, -3)
        startTime[Calendar.HOUR_OF_DAY] = 9
        startTime[Calendar.MINUTE] = 0

        var endTime = Calendar.getInstance()
        endTime.add(Calendar.DAY_OF_YEAR, -3)
        endTime[Calendar.HOUR_OF_DAY] = 10
        endTime[Calendar.MINUTE] = 30

        db.collection("visits").document("visit1").set(
            Visit(Timestamp(startTime.time), Timestamp(endTime.time),
                "TCC", "user1", true, "1")
        )

        startTime.add(Calendar.DAY_OF_YEAR, 3)
        startTime[Calendar.HOUR_OF_DAY] = 11
        startTime[Calendar.MINUTE] = 0

        endTime.add(Calendar.DAY_OF_YEAR, 3)
        endTime[Calendar.HOUR_OF_DAY] = 12
        endTime[Calendar.MINUTE] = 10

        db.collection("visits").document("visit2").set(
            Visit(Timestamp(startTime.time), Timestamp(endTime.time),
                "TCC", "user2", false, "1")
        )

        Thread.sleep(1000)

        // run the contact tracing finding function
        ProfileFragment.queryForContact(db.collection("visits"),
            hashSetOf("TCC"), "user2", ::contactTracingCallback)

        Awaitility.await().until(checkNullityOfContactVisits())

        // check for contacts (potentially other random visits)
        var found = false
        for (visit in contactVisits!!) {
            if (visit.userID == "user1") {
                found = true
                break
            }
        }

        // Assert if we found it
        Assert.assertFalse(found)


        // clean up (delete the visits we just made)
        db.collection("visits").document("visit1").delete()
        db.collection("visits").document("visit2").delete()
    }


    @Test
    fun testContactTracing2Contacts() {
        // User 1 positive visit
        var startTime = Calendar.getInstance()
        startTime[Calendar.HOUR_OF_DAY] = 9
        startTime[Calendar.MINUTE] = 0

        var endTime = Calendar.getInstance()
        endTime[Calendar.HOUR_OF_DAY] = 10
        endTime[Calendar.MINUTE] = 30

        db.collection("visits").document("visit1").set(
            Visit(Timestamp(startTime.time), Timestamp(endTime.time),
                "TCC", "user1", true, "1")
        )

        // User 3 positive visit (diff building)
        startTime[Calendar.HOUR_OF_DAY] = 8
        startTime[Calendar.MINUTE] = 0

        endTime[Calendar.HOUR_OF_DAY] = 9
        endTime[Calendar.MINUTE] = 30

        db.collection("visits").document("visit3").set(
            Visit(Timestamp(startTime.time), Timestamp(endTime.time),
                "SGM", "user3", true, "1")
        )

        // user 2 two visits
        startTime[Calendar.HOUR_OF_DAY] = 11
        startTime[Calendar.MINUTE] = 0

        endTime[Calendar.HOUR_OF_DAY] = 12
        endTime[Calendar.MINUTE] = 10

        db.collection("visits").document("visit2").set(
            Visit(Timestamp(startTime.time), Timestamp(endTime.time),
                "TCC", "user2", false, "1")
        )

        startTime[Calendar.HOUR_OF_DAY] = 8
        startTime[Calendar.MINUTE] = 0

        endTime[Calendar.HOUR_OF_DAY] = 10
        endTime[Calendar.MINUTE] = 30

        db.collection("visits").document("visit4").set(
            Visit(Timestamp(startTime.time), Timestamp(endTime.time),
                "SGM", "user2", false, "1")
        )



        Thread.sleep(1000)

        // run the contact tracing finding function
        ProfileFragment.queryForContact(db.collection("visits"),
            hashSetOf("TCC", "SGM"), "user2", ::contactTracingCallback)

        Awaitility.await().until(checkNullityOfContactVisits())

        // check for contacts (potentially other random visits)
        var foundUser1 = false
        var foundUser3 = false

        for (visit in contactVisits!!) {
            if (visit.userID == "user1") {
                foundUser1 = true
            }
            if (visit.userID == "user3") {
                foundUser3 = true
            }
        }

        // Assert if we found it
        Assert.assertTrue(foundUser1)
        Assert.assertTrue(foundUser3)


        // clean up (delete the visits we just made)
        db.collection("visits").document("visit1").delete()
        db.collection("visits").document("visit2").delete()
    }


}