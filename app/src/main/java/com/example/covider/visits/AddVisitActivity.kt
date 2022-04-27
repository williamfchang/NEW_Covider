package com.example.covider.visits

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.covider.MainActivity
import com.example.covider.R
import com.example.covider.TAG
import com.example.covider.models.*
import com.example.covider.services.MessagingService
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.*


class AddVisitActivity : AppCompatActivity() {
    private lateinit var buildingSpinner: Spinner
    private lateinit var startPicker : TimePicker
    private lateinit var endPicker : TimePicker

    private lateinit var sanitizerSwitch: Switch
    private lateinit var maskSeekBar: SeekBar

    private lateinit var courseLayout: LinearLayout
    private lateinit var courseSwitch: Switch
    private lateinit var courseSection: EditText
    private lateinit var checkBoxM: CheckBox
    private lateinit var checkBoxT: CheckBox
    private lateinit var checkBoxW: CheckBox
    private lateinit var checkBoxTh: CheckBox
    private lateinit var checkBoxF: CheckBox
    private lateinit var addButton: Button

    private lateinit var building: String
    private var startTime : Timestamp? = null
    private var endTime : Timestamp? = null
    private lateinit var courseTitle : EditText

    // firebase
    private val auth = Firebase.auth
    private val db = Firebase.firestore
    private val ms = MessagingService()

    // user model needed for adding Course
    private lateinit var user: User

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_visit)

        // Set up building spinner and selection
        buildingSpinner = findViewById(R.id.spinner_building)
        if (buildingSpinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, resources.getStringArray(R.array.Buildings)
            )
            buildingSpinner.adapter = adapter
        }
        var buildingID = intent.getStringExtra("buildingID")!!
        val buildingIndex = getIndex(buildingSpinner, buildingID)
        buildingSpinner.setSelection(buildingIndex)
        building = resources.getStringArray(R.array.Buildings)[buildingIndex]

        // Set up time pickers
        setTimePickerDefaults()

        // Set up safety measure switches
        sanitizerSwitch = findViewById(R.id.switch_sanitizer)
        maskSeekBar = findViewById(R.id.seekBar_masks)

        // Set up course defaults
        setCourseDefaults()
        courseSwitch.setOnCheckedChangeListener { _, isChecked ->
            val params = courseLayout.layoutParams
            if (isChecked) {
                courseLayout.visibility = View.VISIBLE
                params.height = LinearLayout.LayoutParams.WRAP_CONTENT
            } else {
                courseLayout.visibility = View.INVISIBLE
                params.height = 0
            }
            courseLayout.layoutParams = params
        }

        checkBoxM = findViewById(R.id.checkBoxMonday)
        checkBoxT = findViewById(R.id.checkBoxTuesday)
        checkBoxW = findViewById(R.id.checkBoxWednesday)
        checkBoxTh = findViewById(R.id.checkBoxThursday)
        checkBoxF = findViewById(R.id.checkBoxFriday)

        // Set up add visit button
        addButton = findViewById(R.id.button_add_visit)
        addButton.setOnClickListener {
            startTime = getTime(startPicker)
            endTime = getTime(endPicker)
            if (startTime!!.seconds >= endTime!!.seconds) {
                Toast.makeText(this, "Please select a valid time range", Toast.LENGTH_LONG).show()
                Log.i(TAG(), "Please select a valid time range")
            } else {
                // Add visit to visit list
                val userID = auth.currentUser!!.uid
                buildingID = buildingSpinner.selectedItem.toString()
                val title = courseTitle.text.toString()
                val section = courseSection.text.toString()
                val visit = Visit(startTime, endTime, buildingID.toString(), userID, false, section)

                VisitList.addVisit(visit)

                // Add visit to Firebase
                db.collection("visits").add(visit)


                // Get user survey building safety info
                val hasSanitizer = sanitizerSwitch.isChecked
                val maskPercentage = maskSeekBar.progress

                // Create building report object and add to Firebase
                val newReport = BuildingReport(buildingID, startTime, hasSanitizer, maskPercentage/100.0, userID)
                db.collection("buildingReports").add(newReport)


                // if visit is a course, add course to Firebase
                // first get the user to determine which list to add to
                if (courseSwitch.isChecked && title.isNotBlank() && section.isNotBlank()){
                    var authUser = auth.currentUser
                    val userDocRef = db.collection("users").document(authUser!!.uid)
                    userDocRef.get()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                user = it.result.toObject<User>()!!
                                addCourseToFirebase(user, section, title, buildingID)

                                // subscribe the device to notifications for this course
                                Log.i(TAG(), "going into ms")
                                ms.subscribeToCourse(section)
                            }
                        }
                }

                // Return to main page
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("frag", "visits")
                startActivity(intent)
            }
        }
    }

    private fun addCourseToFirebase(user: User, section: String, title: String, buildingID: String) {
        // check to see if the course already exists
        val courseDocRef = db.collection("courses").document(section)
        courseDocRef.get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful && task.result.exists()) {
                    Log.i(
                        TAG(),
                        "Found course with section: $section! Updating user lists..."
                    )

                    // update the appropriate list in the course
                    if (user.role == CoviderEnums.UserType.STUDENT) {
                        courseDocRef.update(
                            "students",
                            FieldValue.arrayUnion(IdAndName(user.uid, user.name))
                        )
                    } else if (user.role == CoviderEnums.UserType.INSTRUCTOR) {
                        courseDocRef.update(
                            "instructors",
                            FieldValue.arrayUnion(IdAndName(user.uid, user.name))
                        )
                    }
                } else {
                    Log.i(
                        TAG(),
                        "Didn't find course with section: $section, so uploading new course"
                    )

                    // create a new course with a new student
                    var c = Course(section, title, buildingID, getDays())
                    if (user.role == CoviderEnums.UserType.STUDENT) {
                        c.students.add(IdAndName(user.uid, user.name))
                    } else if (user.role == CoviderEnums.UserType.INSTRUCTOR) {
                        c.instructors.add(IdAndName(user.uid, user.name))
                    }

                    // upload the new course
                    db.collection("courses").document(section).set(c)
                }
            }

        // add this course to the user's list of courses (arrayUnion adds this new section to Firebase's current array)
        db.collection("users").document(user.uid).update("courses",FieldValue.arrayUnion(section))

    }


    private fun getTime(p: TimePicker) : Timestamp {
        val c = Calendar.getInstance()
        c[Calendar.HOUR_OF_DAY] = p.hour
        c[Calendar.MINUTE] = p.minute
        val ts = Timestamp(c.time)
        return ts
    }

    private fun setTimePickerDefaults() {
        startPicker = findViewById(R.id.picker_start_time)
        startPicker.setIs24HourView(true);
        startPicker.hour = 12
        startPicker.minute = 0
        endPicker = findViewById(R.id.picker_end_time)
        endPicker.setIs24HourView(true);
        endPicker.hour = 13
        endPicker.minute = 30

        val c = Calendar.getInstance()
        c[Calendar.HOUR_OF_DAY] = 12
        c[Calendar.MINUTE] = 0
        startTime = Timestamp(c.time)
        c[Calendar.HOUR_OF_DAY] = 13
        c[Calendar.MINUTE] = 30
        endTime = Timestamp(c.time)
    }

    private fun setCourseDefaults() {
        courseTitle = findViewById(R.id.input_course_code)
        courseSection = findViewById(R.id.input_course_section)
        courseLayout = findViewById(R.id.course_layout)
        courseSwitch = findViewById(R.id.switch_class)
        courseLayout.visibility = View.INVISIBLE
        val params = courseLayout.layoutParams
        params.height = 0
        courseLayout.layoutParams = params
    }
    private fun getDays(): List<Boolean> {
        return listOf(checkBoxM.isChecked, checkBoxT.isChecked,
            checkBoxW.isChecked, checkBoxTh.isChecked, checkBoxF.isChecked)
    }

    private fun getIndex(spinner: Spinner, string : String): Int {
        for (i in 0..spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == string){
                return i;
            }
        }
        return 0;
    }

}