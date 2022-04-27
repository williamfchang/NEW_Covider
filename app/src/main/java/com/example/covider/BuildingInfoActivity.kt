package com.example.covider

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.example.covider.models.BuildingReport
import com.example.covider.models.Visit
import com.example.covider.visits.AddVisitActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.*

class BuildingInfoActivity : AppCompatActivity() {
    private lateinit var addButton: Button
    private lateinit var buildingIDText: TextView
    private lateinit var buildingNameText: TextView
    private lateinit var buildingAddressText: TextView
    private lateinit var buildingRiskText: TextView

    // firestore
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_info)

        val buildingID = intent.getStringExtra("buildingID")
        val buildingName= intent.getStringExtra("buildingName")
        val buildingAddress= intent.getStringExtra("buildingAddress")

        addButton = findViewById(R.id.button_add_building_visit)
        addButton.setOnClickListener {
            val intent = Intent(this, AddVisitActivity::class.java)
            intent.putExtra("buildingID", buildingID)
            startActivity(intent)
        }
        buildingIDText = findViewById(R.id.building_id_label)
        buildingIDText.text = buildingID
        buildingNameText = findViewById(R.id.building_name_label)
        buildingNameText.text = buildingName
        buildingAddressText = findViewById(R.id.building_address_label)
        buildingAddressText.text = buildingAddress
        buildingRiskText = findViewById(R.id.risk_level_label)
        getBuildingRisk(buildingID!!)
    }

    private fun getBuildingRisk(buildingID: String) {
        var posRate = 0.0
        var avgMaskRate = 0.0
        var avgSanitizer = 0.0
        var posCases = 0.0

        // only consider reports within last 3 days
        val threeDaysBefore = Calendar.getInstance()
        threeDaysBefore.add(Calendar.DAY_OF_YEAR, -3)

        // Find previous building health reports
        val buildingVisits = db.collection("visits")
            .whereEqualTo("buildingID", buildingID)
            .whereGreaterThanOrEqualTo("endTime", Timestamp(threeDaysBefore.time))

        buildingVisits.get().addOnCompleteListener { it ->
            if (it.isSuccessful) {
                posCases = 0.0
                var numVisits = 0.0
                for (doc in it.result) {
                    Log.i(TAG(),"visit logged")
                    val visit: Visit = doc.toObject()
                    if (visit.userWasPositive) {
                        Log.i(TAG(),"visit positive")
                        posCases++
                    }
                    numVisits++
                }
                posRate = posCases / numVisits
                updateText(calculateRisk(posRate, avgMaskRate, avgSanitizer, posCases))
            }
        }

        // Find previous building reports
        val buildingReports = db.collection("buildingReports")
            .whereEqualTo("buildingID", buildingID)
            .whereGreaterThanOrEqualTo("date", Timestamp(threeDaysBefore.time))

        buildingReports.get().addOnCompleteListener { it ->
            if (it.isSuccessful) {
                var numReports = 0.0
                var maskRate = 0.0
                var sanitizer = 0.0
                for (doc in it.result) {
                    val buildingR: BuildingReport = doc.toObject()
                    maskRate += buildingR.maskPercentage!!
                    if (buildingR.sanitizer!!) {
                        sanitizer++
                    }
                    numReports++
                }
                avgMaskRate = maskRate / numReports
                avgSanitizer = sanitizer / numReports
                updateText(calculateRisk(posRate, avgMaskRate, avgSanitizer, posCases))
            } else {
                Log.i(TAG(),"buldng reports not workinig")
            }
        }
    }

    private fun calculateRisk (posRate: Double, maskRate: Double, sanitizerRate: Double, posCases: Double) : String {
        val riskValue : Double = 85*posRate + 10*(1-maskRate) + 5*(1-sanitizerRate)
        var string = ""
        if (riskValue > 70) {
            string += "Extreme."
        } else if (riskValue > 50) {
            string += "High."
        } else if (riskValue > 20) {
            string += "Moderate."
        } else {
            string += "Low."
        }
        string += " There have been " + posCases.toInt() + " covid cases in the past 3 days."
        return string
    }

    private fun updateText(text : String) {
        buildingRiskText.text = text
    }
}

