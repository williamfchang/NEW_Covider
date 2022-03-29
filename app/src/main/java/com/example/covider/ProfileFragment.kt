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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class ProfileFragment : Fragment() {

    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()
    private lateinit var user: User

    private lateinit var linearLayout: LinearLayout
    private lateinit var name: TextView
    private lateinit var userType: TextView
    private lateinit var uscID: TextView
    private lateinit var email: TextView
    private lateinit var courseTable: LinearLayout
    private lateinit var hReportTable: LinearLayout
    private lateinit var addHealthReportButton: Button
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

        addHealthReportButton = view.findViewById(R.id.button_upload_health_reports)
        addHealthReportButton.setOnClickListener {
            val intent = Intent(activity, HealthReportActivity::class.java)
            startActivity(intent)
        }

        loginButton = view.findViewById(R.id.button_profile_login)
        loginButton.visibility = View.GONE
        loginButton.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }

        // Grab and display the text necessary for this Fragment from Firebase
        val authUser = auth.currentUser
        if (authUser != null){

            // get user
            val userDocRef = db.collection("users").document(authUser.uid)
            userDocRef.get()
                .addOnCompleteListener {  task ->
                    if (task.isSuccessful){
                        val document = task.result
                        user = document.toObject(User::class.java)!!
                        name.text = user.name
                        userType.text = user.role.name
                        uscID.text = user.uscID
                        email.text = user.email

                        // print out user classes in table
                        displayUserCourses(courseTable, user.courses)

                        // retrieve and print out user health reports in table
                        val userHealthReports = mutableListOf<HealthReport>()
                        val hReportDocRef = db.collection("healthReports").whereEqualTo("uid", authUser.uid)
                        hReportDocRef.get()
                            .addOnSuccessListener{ documents ->
                                for (d in documents){
                                    userHealthReports.add(d.toObject(HealthReport::class.java))
                                }
                                displayUserHealthReports(hReportTable, userHealthReports)
                            }
                            .addOnFailureListener { exception ->
                                Log.w(TAG(), "Error getting Health Report Documents: ", exception)
                                displayUserHealthReports(hReportTable, userHealthReports)
                            }
                    }
                    else{
                        onFirestoreGetFailed("Failed to retrieve user info")
                    }
                }
        }
        else{
            linearLayout.visibility = View.GONE
            loginButton.visibility = View.VISIBLE
        }

        return view
    }

    private fun displayUserCourses(courseTable: LinearLayout, courses: List<String>){
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

    private fun displayUserHealthReports(hReportTable: LinearLayout, hReports: List<HealthReport>){
        val tableParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val rowParams: TableRow.LayoutParams =
            TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

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
            date.text = hr.date.toString()

            val numSymptoms = TextView(context)
            numSymptoms.layoutParams = rowParams
            numSymptoms. text = hr.numSymptoms.toString()

            val testResult = TextView(context)
            testResult.layoutParams = rowParams
            testResult.text = hr.testedPositive.toString()

            tableRow.addView(date)
            tableRow.addView(numSymptoms)
            tableRow.addView(testResult)
            hReportTable.addView(tableRow)
        }
    }

    private fun onFirestoreGetFailed(errorMsg: String) {
        Log.e(TAG(), errorMsg)
        Toast.makeText(activity, errorMsg, Toast.LENGTH_LONG).show()
    }

}