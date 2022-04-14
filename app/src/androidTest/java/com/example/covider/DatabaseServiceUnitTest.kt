package com.example.covider

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.covider.models.*
import com.example.covider.services.DatabaseService
import com.google.android.gms.tasks.Tasks.await
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.awaitility.Awaitility
import org.awaitility.Duration
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class DatabaseServiceUnitTest {

    private lateinit var db: FirebaseFirestore
    private lateinit var ds: DatabaseService
    private lateinit var auth: FirebaseAuth

    private val maxTestUsers = 4
    private val maxTestCourses = 4
    private val maxTestBuildings = 4

    // default parameters for users, courses, and buildings //
    private fun testEmail(i: Int): String = "tester${i}@test.org"
    private fun testName(i: Int): String = "tester$i"
    private val testPassword = "password"
    private val testUSCID = "123456789"

    private fun testSection(i: Int): String {
        if (i < 0 || i > maxTestCourses){
            Log.e(TAG(), "tried to create course with section outside of 0..$maxTestCourses")
            return "section0"
        }
        return "section$i"
    }

    private var retrievedUser: User? = null
    private var retrievedCourse: Course? = null
    private var retrievedBuilding: Building? = null

    // helper functions to retrieve the model from the Firestore call //
    private fun retrieveTestUser(user: User?){
        Log.i(TAG(), "User ${user!!.email} stored")
        retrievedUser = user
    }
    private fun retrieveTestCourse(course: Course?){
        retrievedCourse = course
    }
    private fun retrieveTestBuilding(building: Building?){
        retrievedBuilding = building
    }

    // helper functions to create test users, courses, and buildings //
    private fun createTestUser(user: User, password: String){
        auth.createUserWithEmailAndPassword(user.email, password)
            .addOnCompleteListener { task ->
                if (task.exception != null) {
                    Log.i(TAG(),"Test user ${user.email} was already created. Attempting to clear user info: ", task.exception)
                    clearExistingTestUser(user, password, true)
                }
                else {
                    val uid = task.result.user!!.uid
                    user.uid = uid

                    db.collection("users").document(uid).set(user)
                    Log.i(TAG(), "Test user ${user.email} created")
                }
            }
    }

    private fun clearExistingTestUser(user: User, password: String, createUser: Boolean){
        auth.signInWithEmailAndPassword(user.email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = task.result.user!!.uid
                    db.collection("users").document(uid).delete()
                    auth.currentUser!!.delete()
                    auth.signOut()
                    Log.i(TAG(),"Deleted test user ${user.email}")

                    if (createUser){
                        createTestUser(user, password)
                    }
                } else {
                    Log.i(TAG(),"Failed to delete test user ${user.email}")
                }
            }
    }

    private fun addTestCourse(course: Course) {
        db.collection("courses").document(course.section).set(course)
    }

    private fun addTestCourseToUser(user: User, section: String){
        val c: Course
        val userList = mutableListOf(IdAndName(user.uid, user.name))
        c = when (user.role){
            CoviderEnums.UserType.STUDENT -> Course(section = section, students = userList)
            CoviderEnums.UserType.INSTRUCTOR -> Course(section = section, instructors = userList)
            else -> Course(section = section)
        }

        // updates the existing course object with students/instructors or creates a new course
        // -> need to delete Courses after every test
        db.collection("courses").document(section).set(c, SetOptions.merge())
    }

    private fun clearTestCourses(){
        for (i in 0..maxTestCourses){
            db.collection("courses").document(testSection(i)).delete()
                .addOnSuccessListener { Log.d(TAG(), "Deleted course $i successfully") }
                .addOnFailureListener { e -> Log.w(TAG(), "course $i didn't exist", e)}
        }
    }

    // Initializing and Destructing code //
    @Before
    fun setUp(){
        db = Firebase.firestore
        ds = DatabaseService(db)
        auth = Firebase.auth
    }

    @After
    fun tearDown() {
        clearTestCourses()
        //TODO: Clear all test users as well
        retrievedUser = null
        retrievedCourse = null
        retrievedBuilding = null
    }

    // Tests //

    // Conditions //
    private fun authFinished(): Callable<Boolean> {
        return Callable<Boolean> { auth.currentUser != null } // The condition that must be fulfilled
    }

    private fun userIsRetrieved(): Callable<Boolean> {
        return Callable<Boolean> { retrievedUser != null } // The condition that must be fulfilled
    }
    @Test
    fun retrieveLoggedInUserTest(){
        val s1 = User(testName(0), testEmail(0), testUSCID, CoviderEnums.UserType.STUDENT)
        createTestUser(s1, testPassword)

        Awaitility.await().until(authFinished())
        ds.retrieveCurrentUser(::retrieveTestUser)

        Awaitility.await().until(userIsRetrieved())
        Assert.assertNotNull(retrievedUser)
        Assert.assertEquals(s1.name, retrievedUser!!.name)
        Assert.assertEquals(s1.email, retrievedUser!!.email)
        Assert.assertEquals(s1.uscID, retrievedUser!!.uscID)
        Assert.assertEquals(s1.role, retrievedUser!!.role)

    }

    @Test
    fun attemptRetrieveLoggedOutUserTest(){
        ds.retrieveCurrentUser(::retrieveTestUser)
        Assert.assertNull(retrievedUser)
    }

    // Conditions //
    private fun courseIsRetrieved(): Callable<Boolean> {
        return Callable<Boolean> { retrievedCourse != null } // The condition that must be fulfilled
    }

    @Test
    fun retrieveEmptyCourseTest(){
        val section = testSection(0)
        val c = Course(section = section)

        db.collection("courses").document(section).set(c)
            .addOnCompleteListener { result ->
                if (result.isSuccessful){
                    ds.retrieveCourse(section, ::retrieveTestCourse)
                }
                else{
                    Log.e(TAG(), "tried to create course with section: ${c.section}")
                }
            }

        Awaitility.await().until(courseIsRetrieved())
        Assert.assertEquals(c.section, retrievedCourse!!.section)
        Assert.assertEquals(c.title, retrievedCourse!!.title)
        Assert.assertEquals(c.mode, retrievedCourse!!.mode)
    }

    @Test
    fun retrieveInvalidCourseTest(){
        val section = "invalid"
        ds.retrieveCourse(section, ::retrieveTestCourse)

        Awaitility.await().until(courseIsRetrieved())
        Assert.assertNotNull(retrievedCourse)
        Assert.assertEquals(retrievedCourse!!.section, "invalid")
    }

    // Conditions //
    private var retrievedHealthStats: MutableList<HealthReport>? = null
    private fun retrieveTestHealthStats(healthStats: MutableList<HealthReport>?){
        retrievedHealthStats = healthStats
    }
    private fun healthStatsAreRetrieved(): Callable<Boolean> {
        return Callable<Boolean> { retrievedHealthStats != null } // The condition that must be fulfilled
    }

    @Test
    fun retrieveEmptyListOfUserHealthStatsTest(){
        val s1 = User(testName(0), testEmail(0), testUSCID, CoviderEnums.UserType.STUDENT)
        createTestUser(s1, testPassword)

        Awaitility.await().until(authFinished())
        ds.retrieveListOfUserHealthStats(::retrieveTestHealthStats)

        Awaitility.await().until(healthStatsAreRetrieved())
        Assert.assertNotNull(retrievedHealthStats)
        Assert.assertEquals(retrievedHealthStats!!.size, 0)
    }

    @Test
    fun retrieveListOfUserHealthStatsTest(){
        val s1 = User(testName(1), testEmail(1), testUSCID, CoviderEnums.UserType.STUDENT)
        createTestUser(s1, testPassword)

        // add health report
        Awaitility.await().until(authFinished())
        val uid = auth.currentUser!!.uid
        val newReport = HealthReport(Timestamp.now(), 1, true, uid)
        val docRef = db.collection("healthReports").add(newReport)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    ds.retrieveListOfUserHealthStats(::retrieveTestHealthStats)
                }
                else{
                    Log.e(TAG(), "tried to create health report for user: ${s1.email}")
                }
            }

        Awaitility.await().atMost(Duration(15, TimeUnit.SECONDS)).until(healthStatsAreRetrieved())
        Assert.assertNotNull(retrievedHealthStats)
        Assert.assertEquals(retrievedHealthStats!!.size, 1)

        Thread.sleep(1000) // wait for 1 second before deleting report
        db.collection("healthReports").document(docRef.result.id).delete()
    }
}