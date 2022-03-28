package com.example.covider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import com.example.covider.models.HealthReport

import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


class HealthReportActivity : AppCompatActivity() {
    private var symptomCount = 0

    // firebase
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_report)
    }

    fun onSymptomCheckboxClicked(view: View) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            // Don't really care which symptom, so just update count
            if (checked) {
                symptomCount++
            }
            else {
                symptomCount--
            }

            Log.i(TAG(), "# of symptoms: $symptomCount")
        }
    }

    fun onTestResultCheckboxClicked(view: View) {
        // currently empty
    }

    fun onVerifyCheckboxClicked(view: View) {
        // Need to verify before submitting
        if (view is CheckBox) {
            findViewById<Button>(R.id.submit_attestation).isEnabled = view.isChecked
        }
    }

    fun onSubmit(view: View) {
        // Get test result
        val isPositive = findViewById<CheckBox>(R.id.checkbox_positive_result).isChecked

        // Verify that we have a current user (otherwise, do nothing)
        val currUser = auth.currentUser

        if (currUser != null) {
            val uid = currUser.uid

            // Create HealthReport object and add to Firebase healthReports database
            val newReport = HealthReport(Timestamp.now(), symptomCount, isPositive, uid)
            db.collection("healthReports").add(newReport)

            // given positive test result, mark all visits in last 3 days as positive
            if (isPositive) {
                // get a date for 3 days before
                val threeDaysBefore = Calendar.getInstance()
                threeDaysBefore.add(Calendar.DAY_OF_YEAR, -3)

                // get subset Firebase visits collection based on query (same userID within 3 days)
                val visitsRef = db.collection("visits")
                val recentVisitsOfUser = visitsRef
                    .whereEqualTo("userID", uid)
                    .whereGreaterThanOrEqualTo("startTime", Timestamp(threeDaysBefore.time))

                // go through each visit and update userWasPositive field to true
                recentVisitsOfUser.get()
                    .addOnSuccessListener { result ->
                        for (doc in result) {
                            doc.reference.update("userWasPositive", true)
                        }

                        Log.i(TAG(), "Successfully modified visits with positive test result")
                    }
                    .addOnFailureListener { it ->
                        Log.i(TAG(), "Failed to open visits collection", it)
                    }
            }
        }
        else {
            Toast.makeText(this, "Unsuccessful: no current user", Toast.LENGTH_LONG).show()
        }

        finish()
    }
}