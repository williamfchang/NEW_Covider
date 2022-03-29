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
import com.example.covider.models.Visit
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


class AddVisitActivity : AppCompatActivity() {
    private lateinit var buildingSpinner: Spinner
    private lateinit var startPicker : TimePicker
    private lateinit var endPicker : TimePicker
    private lateinit var courseSwitch: Switch
    private lateinit var addButton: Button

    private lateinit var building: String
    private var startTime : Timestamp? = null
    private var endTime : Timestamp? = null
    private lateinit var courseIDText : EditText

    // firebase
    private val auth = Firebase.auth
    private val db = Firebase.firestore

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
        courseIDText = findViewById(R.id.input_course_code)
        courseIDText.visibility = View.INVISIBLE
        courseSwitch = findViewById(R.id.switch_class)
        courseSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                courseIDText.visibility = View.VISIBLE
            } else {
                courseIDText.visibility = View.INVISIBLE
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
                val course = courseIDText.text.toString() // TODO: check if valid
                val visit = Visit(startTime, endTime, buildingID.toString(), userID, false, course)
                VisitList.addVisit(visit)

                // Add visit to Firebase
                db.collection("visits").add(visit)

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