package com.example.covider

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.covider.models.*
import com.example.covider.services.DatabaseService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CourseViewActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener{
    private lateinit var ds: DatabaseService
    private lateinit var db: FirebaseFirestore
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
    private lateinit var newCourseMode: CoviderEnums.ClassMode

    private val numCasesOneDay: MutableSet<String> =  mutableSetOf()
    private val numCasesOneWeek: MutableSet<String> = mutableSetOf()
    private val numCasesOneMonth: MutableSet<String> = mutableSetOf()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_course_info)

        val userRole = intent.getStringExtra("userRole")
        val courseSection = intent.getStringExtra("courseID")

        viewCourseId = findViewById(R.id.view_course_id)
        viewCourseMode = findViewById(R.id.course_node)
        viewInstructor = findViewById(R.id.view_instructor)
        studentTable = findViewById(R.id.table_students)
        classHealthReportTable = findViewById(R.id.table_course_health_reports)
        viewDailyCourseCases = findViewById(R.id.view_daily_course_cases)
        viewWeeklyCourseCases = findViewById(R.id.view_weekly_course_cases)
        viewMonthlyCourseCases = findViewById(R.id.view_monthly_course_cases)
        updateCourseModeButton = findViewById(R.id.button_update_course_mode)

        spinner = findViewById(R.id.spinner_course_mode)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, CoviderEnums.ClassMode.values()
        )
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        updateCourseModeButton.setOnClickListener {
            updateCourseMode()
        }

        // if you are not an instructor, you don't have access to these values
        if (userRole != CoviderEnums.UserType.INSTRUCTOR.name){
            findViewById<TextView>(R.id.text_course_health_reports).visibility = View.GONE
            classHealthReportTable.visibility = View.GONE
            updateCourseModeButton.visibility = View.GONE
            spinner.visibility = View.GONE
        }

        // Initialize database service
        ds = DatabaseService(FirebaseFirestore.getInstance())
        db = Firebase.firestore

        // display course information
        ds.retrieveCourse(courseSection!!, ::displayCourseInfo)
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

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        newCourseMode = p0?.getItemAtPosition(p2) as CoviderEnums.ClassMode
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    private fun updateCourseMode(){
        if (viewCourseMode.text != newCourseMode.name) {
            viewCourseMode.text = newCourseMode.name
            val courseSection = viewCourseId.text.split(":")[1]

            // update the mode of the course in the database
            db.collection("courses").document(courseSection).update("mode", newCourseMode)
            Log.i(TAG(), "Mode of $courseSection was updated to ${newCourseMode.name}")
        }
        else{
            Toast.makeText(this, "Course mode unchanged", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayCourseInfo(course: Course){
        viewCourseId.text = "${course.title}:${course.section}"
        viewCourseMode.text = course.mode.name
        spinner.setSelection(course.mode.ordinal)
        var str = ""
        if (course.instructors.isNotEmpty()){
            for (i in course.instructors) {
                str += " ${i.second},"
            }
        }
        else{
            str = "No Instructors Found"
        }

        viewInstructor.text = str
        displayStudents(studentTable, course.students)

        val users = course.instructors+course.students
        ds.getPositiveReportsForUsersInPastNumDays(users, numCasesOneDay, 1, ::updateDailyCases, viewDailyCourseCases)
        ds.getPositiveReportsForUsersInPastNumDays(users, numCasesOneWeek, 7, ::updateWeeklyCases, viewWeeklyCourseCases)
        ds.getPositiveReportsForUsersInPastNumDays(users, numCasesOneMonth, 31, ::updateMonthlyCases, viewMonthlyCourseCases)
    }

    private fun updateDailyCases(view:TextView){
        view.text = numCasesOneDay.size.toString()
    }

    private fun updateWeeklyCases(view:TextView){
        view.text = numCasesOneWeek.size.toString()
    }

    private fun updateMonthlyCases(view:TextView){
        view.text = numCasesOneMonth.size.toString()
    }
}