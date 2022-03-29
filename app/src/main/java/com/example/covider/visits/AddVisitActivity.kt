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
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


class AddVisitActivity : AppCompatActivity() {
    private lateinit var buildingSpinner: Spinner
    private lateinit var startPicker : TimePicker
    private lateinit var endPicker : TimePicker
    private lateinit var courseSwitch: Switch
    private lateinit var courseSection: EditText
    private lateinit var addButton: Button

    private lateinit var building: String
    private var startTime : Timestamp? = null
    private var endTime : Timestamp? = null
    private lateinit var courseTitle : EditText

    // firebase
    private val auth = Firebase.auth
    private val db = Firebase.firestore

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

        // Set up switch and textfield visibility
        courseTitle = findViewById(R.id.input_course_code)
        courseTitle.visibility = View.INVISIBLE

        courseSection = findViewById(R.id.input_course_section)
        courseSection.visibility = View.INVISIBLE

        courseSwitch = findViewById(R.id.switch_class)
        courseSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                courseTitle.visibility = View.VISIBLE
                courseSection.visibility = View.VISIBLE
            } else {
                courseTitle.visibility = View.INVISIBLE
                courseSection.visibility = View.INVISIBLE
            }
        }

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

                // if visit is a course, add course to Firebase
                // first get the user to determine which list to add to
                if (courseSwitch.isChecked && title.isNotBlank() && section.isNotBlank()){

                    var authUser = auth.currentUser
                    val userDocRef = db.collection("users").document(authUser!!.uid)
                    userDocRef.get()
                        .addOnCompleteListener {
                            if (it.isSuccessful){
                                var d = it.result
                                user = d.toObject(User::class.java)!!

                                // check to see if the course already exists
                                val courseDocRef = db.collection("courses").document(section)
                                courseDocRef.get()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful && task.result.exists()) {
                                            Log.i(TAG(), "Found course with section: $section! Updating user lists...")

                                            // update the appropriate list in the course
                                            if (user.role == CoviderEnums.UserType.STUDENT){
                                                courseDocRef.update("students",FieldValue.arrayUnion(IdAndName(authUser.uid, user.name)))
                                            }
                                            else if (user.role == CoviderEnums.UserType.INSTRUCTOR){
                                                courseDocRef.update("instructors",FieldValue.arrayUnion(IdAndName(authUser.uid, user.name)))
                                            }
                                        }
                                        else{
                                            Log.i(TAG(), "Didn't find course with section: $section, so uploading new course")

                                            // create a new course with a new student
                                            var c = Course(section, title)
                                            if (user.role == CoviderEnums.UserType.STUDENT){
                                                c.students.add(IdAndName(authUser.uid, user.name))
                                            }
                                            else if (user.role == CoviderEnums.UserType.INSTRUCTOR){
                                                c.instructors.add(IdAndName(authUser.uid, user.name))
                                            }

                                            // upload the new course
                                            db.collection("courses").document(section).set(c)
                                        }
                                    }
                            }
                        }

                    db.collection("users").document(authUser!!.uid).update("courses",FieldValue.arrayUnion(section))
                }

                // Return to main page
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("frag", "visits")
                // TODO open main activity to visits frag
                startActivity(intent)
            }
        }
    }

    private fun getTime(p: TimePicker) : Timestamp {
        val c = Calendar.getInstance()
        c[Calendar.HOUR_OF_DAY] = p.hour
        c[Calendar.MINUTE] = p.minute
        val ts = Timestamp(c.time)
        return ts
    }

    private fun setTimePickerDefaults(){
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

    private fun getIndex(spinner: Spinner, string : String): Int {
        for (i in 0..spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == string){
                return i;
            }
        }
        return 0;
    }

}