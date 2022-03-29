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
import androidx.appcompat.app.AppCompatActivity
import com.example.covider.models.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.*

class CourseViewActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener{
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var course: Course

    private lateinit var linearLayout: LinearLayout
    private lateinit var viewCourseId: TextView
    private lateinit var viewCourseMode: TextView
    private lateinit var viewInstructor: TextView
    private lateinit var studentTable: LinearLayout
    private lateinit var classHealthReportTable: LinearLayout
    private lateinit var viewDailyCourseCases: TextView
    private lateinit var viewWeeklyCourseCases: TextView
    private lateinit var viewMonthlyCourseCases: TextView
    private lateinit var spinner: Spinner
    private lateinit var updateCourseModeButton: Button
    private lateinit var tmpCourseMode: CoviderEnums.ClassMode

    // get the Course Health Stats for the past few days
    private val numCasesOneDay: MutableSet<String> =  mutableSetOf()
    private val numCasesOneMonth: MutableSet<String> = mutableSetOf()
    private val numCasesOneYear: MutableSet<String> = mutableSetOf()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_info)

        var userRole = intent.getStringExtra("userRole")
        var courseSection = intent.getStringExtra("courseID")

        viewCourseId = findViewById(R.id.view_course_id)
        viewCourseMode = findViewById(R.id.course_node)
        viewInstructor = findViewById(R.id.view_instructor)
        studentTable = findViewById(R.id.table_students)
        classHealthReportTable = findViewById(R.id.table_course_health_reports)
        viewDailyCourseCases = findViewById(R.id.view_daily_course_cases)
        viewWeeklyCourseCases = findViewById(R.id.view_weekly_course_cases)
        viewMonthlyCourseCases = findViewById(R.id.view_monthly_course_cases)
        updateCourseModeButton = findViewById(R.id.button_update_course_mode)
        //updateCourseModeButton.setOnClickListener { createAccount() }

        // if you are not an instructor, you don't have access to these values
        if (userRole != CoviderEnums.UserType.INSTRUCTOR.name){
            findViewById<TextView>(R.id.text_course_health_reports).visibility = View.GONE
            classHealthReportTable.visibility = View.GONE
            updateCourseModeButton.visibility = View.GONE
            spinner.visibility = View.GONE
        }

        spinner = findViewById(R.id.spinner_course_mode)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, CoviderEnums.ClassMode.values()
        )
        spinner.adapter = adapter

        // Initialize Firebase
        db = FirebaseFirestore.getInstance()

//        db.collection("courses").document("2")
//            .set(Course("2",
//                "CSCI104", listOf("M", "W", "F"),
//                mutableListOf(IdAndName("cyACUzs5KBS0yavomplcbd1XjJB3", "Nirav")),
//                mutableListOf(IdAndName("cyACUzs5KBS0yavomplcbd1XjJB3", "Nirav")),
//                CoviderEnums.ClassMode.HYBRID))

        // get course info
        val courseDocRef = db.collection("courses").document(courseSection!!)
        courseDocRef.get()
            .addOnCompleteListener {  task ->
                if (task.isSuccessful) {
                    val document = task.result
                    course = if(document.toObject(Course::class.java) != null){
                        document.toObject(Course::class.java)!!
                    }
                    else{
                        Course("NULL", courseSection)
                    }

                    if (course != null) {
                        viewCourseId.text = course.title + ":" + course.section
                        viewCourseMode.text = course.mode.name
                        var str = ""
                        if (!course.instructors.isEmpty()){
                            for (i in course.instructors) {
                                str += " ${i.second},"
                            }
                        }
                        else{
                            str = "No Instructors Found"
                        }

                        viewInstructor.text = str
                        displayStudents(studentTable, course.students)

                        val oneDayBefore = Calendar.getInstance()
                            oneDayBefore.add(Calendar.DAY_OF_YEAR, -1)
                        val oneMonthBefore = Calendar.getInstance()
                            oneMonthBefore.add(Calendar.DAY_OF_YEAR, -30)
                        val oneYearBefore = Calendar.getInstance()
                            oneYearBefore.add(Calendar.DAY_OF_YEAR, -365)

                        // get subset Firebase visits collection based on query (same userID within 3 days)
                        val reportsRef = db.collection("healthReports")
                        for (s in course.students) {
                            val oneDayClassReports = reportsRef
                                .whereEqualTo("userID", s.first).whereEqualTo("testedPositive", true)
                                .whereGreaterThanOrEqualTo("date", Timestamp(oneDayBefore.time))
                            val oneMonthClassReports = reportsRef
                                .whereEqualTo("userID", s.first).whereEqualTo("testedPositive", true)
                                .whereGreaterThanOrEqualTo("date", Timestamp(oneDayBefore.time))
                            val oneYearClassReports = reportsRef
                                .whereEqualTo("userID", s.first).whereEqualTo("testedPositive", true)
                                .whereGreaterThanOrEqualTo("date", Timestamp(oneDayBefore.time))

                            oneDayClassReports.get()
                                .addOnSuccessListener { result ->
                                    for (d in result.documents){
                                        Log.i(TAG(), "Document: ${d}")
                                        numCasesOneDay.add(d["userID"] as String)
                                    }
                                    Log.i(TAG(), "Successfully modified one day positive cases for course")
                                    viewDailyCourseCases.text = numCasesOneDay.size.toString()
                                }
                                .addOnFailureListener { it ->
                                    Log.i(TAG(), "Failed to open visits collection", it)
                                }
                        }
                    }
                }
                else{
                    onFirestoreGetFailed("Failed to retrieve course info")
                }
            }
    }

    private fun displayStudents(studentTable: LinearLayout, students: MutableList<IdAndName>) {
        val tableParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        val rowParams: TableRow.LayoutParams =
            TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
            )

        if (students.isEmpty()){
            val tableRow = TableRow(this)
            tableRow.layoutParams = tableParams // TableLayout is the parent view
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            val textView = TextView(this)
            textView.layoutParams = rowParams // TableRow is the parent view
            textView.text = "No Students Found"

            tableRow.addView(textView)
            studentTable.addView(tableRow)
        }

        for (s in students){
            val tableRow = TableRow(this)
            tableRow.layoutParams = tableParams // TableLayout is the parent view
            tableRow.gravity = Gravity.CENTER_HORIZONTAL

            val textView = TextView(this)
            textView.layoutParams = rowParams // TableRow is the parent view
            textView.text = s.second

            tableRow.addView(textView)
            studentTable.addView(tableRow)
        }
    }

    private fun onFirestoreGetFailed(errorMsg: String) {
        Log.e(TAG(), errorMsg)
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show()
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        tmpCourseMode = CoviderEnums.ClassMode.valueOf(p0?.getItemAtPosition(p2) as String)
        // update the mode of the course in the database
        Log.i(TAG(), tmpCourseMode.name)
    }

    private fun updateMode(mode: CoviderEnums.ClassMode){

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")

    }
}