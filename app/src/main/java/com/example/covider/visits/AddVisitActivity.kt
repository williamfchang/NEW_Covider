package com.example.covider.visits

import android.app.TimePickerDialog
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


class AddVisitActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener,
    TimePicker.OnTimeChangedListener {
    private lateinit var spinner: Spinner
    private lateinit var startPicker : TimePicker
    private lateinit var endPicker : TimePicker
    private lateinit var switch: Switch
    private lateinit var textField: EditText
    private lateinit var addButton: Button

    private lateinit var building: String
    private var startTime : Timestamp? = null
    private var endTime : Timestamp? = null
    private lateinit var courseID : EditText

    // firebase
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_visit)

        spinner = findViewById(R.id.spinner_building)
        if (spinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, resources.getStringArray(R.array.Buildings)
            )
            spinner.adapter = adapter
        }
        var buildingID: Int = intent.getIntExtra("Building Index", 0)
        spinner.setSelection(buildingID)
        building = resources.getStringArray(R.array.Buildings)[buildingID]

        setTimePickerDefaults()

        textField = findViewById(R.id.input_course_code)
        textField.visibility = View.INVISIBLE
        switch = findViewById(R.id.switch_class)
        switch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                textField.visibility = View.VISIBLE
            } else {
                textField.visibility = View.INVISIBLE
            }
        }

        courseID = findViewById(R.id.input_course_code)
        // TODO: Change
        addButton = findViewById(R.id.button_add_visit)
        addButton.setOnClickListener {
            val userID = auth.currentUser!!.uid
            val course = courseID.text.toString()
            val visit = Visit(startTime, endTime, buildingID.toString(), userID, null, course)
            VisitList.addVisit(visit)
            Log.i(TAG(), "${(VisitList.visits).toString()}" )
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        building = resources.getStringArray(R.array.Buildings)[p2]

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

    override fun onTimeChanged(p0: TimePicker?, p1: Int, p2: Int) {
        val c = Calendar.getInstance()
        c[Calendar.HOUR_OF_DAY] = p1
        c[Calendar.MINUTE] = p2
        val ts = Timestamp(c.time)
        if (p0 == startPicker) {
            startTime = ts
        } else if (p0 == endPicker) {
            endTime = ts
        }
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

}