package com.example.covider.services

import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.covider.TAG
import com.example.covider.models.*
import com.google.firebase.auth.ktx.auth
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

class DatabaseService constructor(private val db: FirebaseFirestore){
    private var auth = Firebase.auth

    public fun retrieveCurrentUser(onResult: (User?) -> Unit){
        val authUser = auth.currentUser
        if (authUser != null) {
            val docRef = db.collection("users").document(authUser.uid)
            docRef.get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        Log.i(TAG(), "Successfully retrieved user:${authUser.uid}")
                        onResult(task.result.toObject(User::class.java))
                    }
                }
        }
    }

    public fun retrieveAllBuildings(buildings: ArrayList<Building>, map: GoogleMap, onResult: (map: GoogleMap) -> Unit){
        db.collection("buildings").get()
            .addOnSuccessListener { result ->
                // loop through all building docs to create Building object
                for (doc in result) {
                    buildings.add(doc.toObject<Building>())
                }

                // buildingsLoaded = true
                Log.i(TAG(), "Successfully created ${buildings.size} buildings")

                // Now draw buildings on map
                onResult(map)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG(), "Error getting buildings.", exception)
            }
    }

    public fun retrieveCourse(courseSection: String, onResult: (Course) -> Unit){
        val courseDocRef = db.collection("courses").document(courseSection)
        courseDocRef.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    val course = if (document.toObject(Course::class.java) != null) {
                        document.toObject(Course::class.java)!!
                    } else {
                        Course("invalid", courseSection)
                    }

                    onResult(course)
                }
                else {
                    Log.e(TAG(), "Failed to retrieve course info")
                }
            }
    }

    public fun getPositiveReportsForUsersInPastNumDays(users: List<IdAndName>, numCases: MutableSet<String>, numDays: Int, onResult: (v: TextView) -> Unit, view: TextView){
        // Get the Course Health Stats for the past few days
        val nDaysBefore = Calendar.getInstance()
            nDaysBefore.add(Calendar.DAY_OF_YEAR, -1 * numDays)

        val reportsRef = db.collection("healthReports")
        for (u in users) {
            val nDaysClassReports = reportsRef
                .whereEqualTo("userID", u.first).whereEqualTo("testedPositive", true)
                .whereGreaterThanOrEqualTo("date", Timestamp(nDaysBefore.time))

            nDaysClassReports.get()
                .addOnSuccessListener { result ->
                    for (d in result.documents){
                        Log.i(TAG(), "$numDays - found positive report for user: ${d["userID"] as String}")
                        numCases.add(d["userID"] as String)
                    }
                    Log.i(TAG(), "Successfully modified one day positive cases for course")
                    onResult(view);
                }
                .addOnFailureListener { it ->
                    Log.i(TAG(), "Failed to open visits collection", it)
                }
        }
    }

    public fun uploadUserPersonalData(){

    }

    public fun uploadUserHealthStats(){

    }

    public fun retrieveListOfUserHealthStats(onResult: (MutableList<HealthReport>) -> Unit){
        val authUser = auth.currentUser!!
        val userHealthReports = mutableListOf<HealthReport>()
        val hReportDocRef = db.collection("healthReports").whereEqualTo("userID", authUser.uid)
        hReportDocRef.get()
            .addOnSuccessListener{ documents ->
                for (d in documents){
                    userHealthReports.add(d.toObject(HealthReport::class.java))
                }
                onResult(userHealthReports)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG(), "Error getting Health Report Documents: ", exception)
                onResult(userHealthReports)
            }
    }

    public fun uploadVisit(){

    }

    public fun uploadBuildingVisit(){

    }

    public fun retrieveListOfVisitsForUser(){

    }

    public fun retrieveListOfVisitsForBuilding(){

    }

    public fun retrieveCloseContactVisits(onResult: (MutableList<Visit>) -> Unit) {
//        retrieveBuildingsVisitedInPastNumDays()
    }

    public fun retrieveBuildingsVisitedInPastNumDays(numDays: Int, onResult: (Set<String>) -> Unit) {
        val authUser = auth.currentUser!!

        val nDaysBefore = Calendar.getInstance()
        nDaysBefore.add(Calendar.DAY_OF_YEAR, -1 * numDays)

        val visitsRef = db.collection("visits")
        val recentVisitsOfUserQuery = visitsRef
            .whereEqualTo("userID", authUser.uid)
            .whereGreaterThanOrEqualTo("endTime", Timestamp(nDaysBefore.time))

        // get buildings visited within the past day
        var recentBuildings = HashSet<String>() // "SAL", "TCC", etc.
        recentVisitsOfUserQuery.get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    recentBuildings.add(doc.get("buildingID") as String)
                }

                Log.i(TAG(), "Successfully found recent buildings: $recentBuildings")
                onResult(recentBuildings)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG(), "Error trying to find recent buildings", exception)
            }
    }
}