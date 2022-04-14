package com.example.covider

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.covider.models.HealthReport
import com.example.covider.models.User
import com.example.covider.models.Visit
import com.example.covider.services.DatabaseService
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.*


class ProfileFragment : Fragment() {

    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()
    private val ds = DatabaseService(db)
    private lateinit var user: User

    private lateinit var linearLayout: LinearLayout
    private lateinit var name: TextView
    private lateinit var userType: TextView
    private lateinit var uscID: TextView
    private lateinit var email: TextView
    private lateinit var courseTable: LinearLayout
    private lateinit var hReportTable: LinearLayout
    private lateinit var contactTable: LinearLayout
    private lateinit var addHealthReportButton: Button
    private lateinit var logoutButton: Button
    private lateinit var loginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        linearLayout = view.findViewById(R.id.profile_linear_layout)

        name = view.findViewById(R.id.text_name)
        userType = view.findViewById(R.id.user_type)
        uscID = view.findViewById(R.id.view_id)
        email = view.findViewById(R.id.view_email)

        courseTable = view.findViewById(R.id.table_courses)
        hReportTable = view.findViewById(R.id.table_health_reports)
        contactTable = view.findViewById(R.id.table_contact_tracing)

        // create health report
        addHealthReportButton = view.findViewById(R.id.button_upload_health_reports)
        addHealthReportButton.setOnClickListener {
            val intent = Intent(activity, HealthReportActivity::class.java)
            startActivity(intent)
        }

        // log out
        logoutButton = view.findViewById(R.id.button_logout)
        logoutButton.setOnClickListener {
            Firebase.auth.signOut() // sign out

            // now switch intent
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }

        // go to login page
        loginButton = view.findViewById(R.id.button_profile_login)
        loginButton.visibility = View.GONE
        loginButton.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }

        // Grab and display the text necessary for this Fragment from Firebase
//        ds.retrieveCurrentUser{ user: User? ->
//            if (user != null){
//                displayUserInfo(user)
//            }
//            else {
//                val intent = Intent(activity, LoginActivity::class.java)
//                startActivity(intent)
//            }
//        }

        val authUser = auth.currentUser
        if (authUser != null){

            // get user
            val userDocRef = db.collection("users").document(authUser.uid)
            userDocRef.get()
                .addOnCompleteListener {  task ->
                    if (task.isSuccessful){
                        val document = task.result

                        if (document.toObject(User::class.java) == null){
                            val intent = Intent(activity, LoginActivity::class.java)
                            startActivity(intent)
                        }

                        user = document.toObject(User::class.java)!!
                        name.text = user.name
                        userType.text = user.role.name
                        uscID.text = user.uscID
                        email.text = user.email

                        // print out user classes in table
                        displayUserCourses(user.courses)

                        // retrieve and print out user health reports in table
                        val userHealthReports = mutableListOf<HealthReport>()
                        val hReportDocRef = db.collection("healthReports").whereEqualTo("userID", authUser.uid)
                        hReportDocRef.get()
                            .addOnSuccessListener{ documents ->
                                for (d in documents){
                                    userHealthReports.add(d.toObject(HealthReport::class.java))
                                }
                                displayUserHealthReports(userHealthReports)
                            }
                            .addOnFailureListener { exception ->
                                Log.w(TAG(), "Error getting Health Report Documents: ", exception)
                                displayUserHealthReports(userHealthReports)
                            }
                    }
                    else{
                        onFirestoreGetFailed("Failed to retrieve user info")
                    }
                }

            // -- Contact tracing -- //
            val uid = authUser.uid
            val oneDayBefore = Calendar.getInstance()
            oneDayBefore.add(Calendar.DAY_OF_YEAR, -1)

            // get user's visits in the past day. Copied from HealthReportActivity
            val visitsRef = db.collection("visits")
            val recentVisitsOfUserQuery = visitsRef
                .whereEqualTo("userID", uid)
                .whereGreaterThanOrEqualTo("endTime", Timestamp(oneDayBefore.time))

            // get buildings visited within the past day
            var recentBuildings = HashSet<String>() // "SAL", "TCC", etc.
            recentVisitsOfUserQuery.get()
                .addOnSuccessListener { result ->
                    for (doc in result) {
                        recentBuildings.add(doc.get("buildingID") as String)
                    }

                    Log.i(TAG(), "Successfully found recent buildings: $recentBuildings")
                    getAndDisplayContactVisits(uid, recentBuildings)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG(), "Error trying to find recent buildings", exception)
                }
        }
        else{
            linearLayout.visibility = View.GONE
            loginButton.visibility = View.VISIBLE
        }

        return view
    }

    private fun displayUserInfo(user: User) {
        name.text = user.name
        userType.text = user.role.name
        uscID.text = user.uscID
        email.text = user.email

        // print out user classes in table
        displayUserCourses(user.courses)

        // retrieve and print out health reports
        ds.retrieveListOfUserHealthStats(::displayUserHealthReports)

        // retrieve and print out close contacts
        ds.retrieveCloseContactVisits(::displayContactTracing)

    }

    private fun displayUserCourses(courses: List<String>){
        val tableParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        val rowParams: TableRow.LayoutParams =
            TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

        if (courses.isEmpty()){
            val tableRow = TableRow(context)
            tableRow.layoutParams = tableParams // TableLayout is the parent view
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            val textView = TextView(context)
            textView.layoutParams = rowParams // TableRow is the parent view
            textView.text = "No Courses Found"

            tableRow.addView(textView)
            courseTable.addView(tableRow)
        }

        for (c in courses){
            val tableRow = TableRow(context)
            tableRow.layoutParams = tableParams // TableLayout is the parent view
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            val textView = TextView(context)
            textView.layoutParams = rowParams // TableRow is the parent view
            textView.text = c
            textView.setTextColor(Color.BLUE)

            tableRow.addView(textView)
            tableRow.setOnClickListener {
                val intent = Intent(activity, CourseViewActivity::class.java)
                intent.putExtra("userRole", user.role.name)
                intent.putExtra("courseID", c)
                startActivity(intent)
            }
            courseTable.addView(tableRow)
        }
    }

    private fun displayUserHealthReports(hReports: List<HealthReport>){
        val tableParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val rowParams: TableRow.LayoutParams =
            TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

        // header row
        val rowheader = TableRow(context)
        rowheader.layoutParams = tableParams // TableLayout is the parent view
        rowheader.gravity = Gravity.CENTER_HORIZONTAL

        val headerText = TextView(context)
        headerText.layoutParams = rowParams
        headerText.text = "Date | Num Symptoms | Test Result"

        rowheader.addView(headerText)
        hReportTable.addView(rowheader)

        // remaining rows
        if (hReports.isEmpty()){
            val tableRow = TableRow(context)
            tableRow.layoutParams = tableParams // TableLayout is the parent view
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            val textView = TextView(context)
            textView.layoutParams = rowParams // TableRow is the parent view
            textView.text = "No Health Reports Found"

            tableRow.addView(textView)
            hReportTable.addView(tableRow)
        }

        for (hr in hReports){
            val tableRow = TableRow(context)
            tableRow.layoutParams = tableParams // TableLayout is the parent view
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            val date = TextView(context)
            date.layoutParams = rowParams
            date.text = hr.date!!.toDate().toString()

            val numSymptoms = TextView(context)
            numSymptoms.layoutParams = rowParams
            numSymptoms.text = " | " + hr.numSymptoms.toString() + " | "

            val testResult = TextView(context)
            testResult.layoutParams = rowParams
            testResult.text = hr.testedPositive.toString()

            tableRow.addView(date)
            tableRow.addView(numSymptoms)
            tableRow.addView(testResult)
            hReportTable.addView(tableRow)
        }
    }

    private fun displayContactTracing(contacts: List<Visit>) {
        val tableParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val rowParams: TableRow.LayoutParams =
            TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

        if (contacts.isEmpty()){
            val tableRow = TableRow(context)
            tableRow.layoutParams = tableParams // TableLayout is the parent view
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            val textView = TextView(context)
            textView.layoutParams = rowParams // TableRow is the parent view
            textView.text = "No close contacts found"

            tableRow.addView(textView)
            contactTable.addView(tableRow)
        }

        for (visit in contacts){
            val tableRow = TableRow(context)
            tableRow.layoutParams = tableParams // TableLayout is the parent view
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            val info = TextView(context)
            info.layoutParams = rowParams
            info.text = "${visit.endTime!!.toDate()} @ ${visit.buildingID}"

            tableRow.addView(info)
//            tableRow.addView(buildingCode)
            contactTable.addView(tableRow)
        }
    }


    private fun getAndDisplayContactVisits(uid: String, recentBuildings: HashSet<String>) {
        val oneDayBefore = Calendar.getInstance()
        oneDayBefore.add(Calendar.DAY_OF_YEAR, -1)

        val visitsRef = db.collection("visits")

        // get all global visits in the past day to those buildings that were POSITIVE
        // ...unless empty, then just call displayContactTracing with an empty array
        if (recentBuildings.isEmpty()) {
            Log.i(TAG(), "No recent buildings found, no contact tracing query used")
            displayContactTracing(ArrayList<Visit>())
        }
        else {
            val contactVisitsQuery = visitsRef
                .whereEqualTo("userWasPositive", true)
                .whereGreaterThanOrEqualTo("endTime", Timestamp(oneDayBefore.time))
                .whereIn("buildingID", recentBuildings.toList())

            // Pull these visits out and pass to display function
            var contactVisits = ArrayList<Visit>()

            contactVisitsQuery.get()
                .addOnSuccessListener { result ->
                    for (doc in result) {
                        // Only add if it's not yourself
                        if ((doc.get("userID") as String) != uid) {
                            contactVisits.add(doc.toObject())
                        }
                    }

                    Log.i(TAG(), "Successfully found close contacts: $contactVisits")
                    displayContactTracing(contactVisits)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG(), "Error getting close contact visits.", exception)
                }
        }
    }


    private fun onFirestoreGetFailed(errorMsg: String) {
        Log.e(TAG(), errorMsg)
        Toast.makeText(activity, errorMsg, Toast.LENGTH_LONG).show()
    }

}