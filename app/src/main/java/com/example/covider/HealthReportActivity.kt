package com.example.covider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast


class HealthReportActivity : AppCompatActivity() {
    private var symptomCount = 0
//    private var hasPositiveTestResult = false
//    private var answersVerified = false

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


//            when (view.id) {
//                R.id.checkbox_chills_or_fever -> {
//                    if (checked) {
//                        Log.i(TAG(), "Meat checked")
//                    } else {
//                        // Remove the meat
//                    }
//                }
//                R.id.checkbox_taste_or_smell -> {
//                    if (checked) {
//                        Log.i(TAG(), "Cheese checked")
//                    } else {
//                        // I'm lactose intolerant
//                    }
//                }
//            }
        }
    }

    fun onTestResultCheckboxClicked(view: View) {
        // currently empty
    }

    fun onVerifyCheckboxClicked(view: View) {
        if (view is CheckBox) {
            findViewById<Button>(R.id.submit_attestation).isEnabled = view.isChecked
        }
    }

    fun onSubmit(view: View) {
        // Get test result
        val isPositive = findViewById<CheckBox>(R.id.checkbox_positive_result).isChecked

        Toast.makeText(this, "# of symptoms: $symptomCount. Is positive? $isPositive", Toast.LENGTH_LONG).show()
        finish()
    }
}