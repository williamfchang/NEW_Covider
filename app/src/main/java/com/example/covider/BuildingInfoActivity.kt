package com.example.covider

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.example.covider.models.BuildingReport
import com.example.covider.models.Visit
import com.example.covider.visits.AddVisitActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
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
        buildingRiskText.text = getBuildingRisk()
    }

    private fun getBuildingRisk() : String {
        var posRate = 0.0
        var avgMaskRate = 0.0
        var avgSanitizer = 0.0
        var posCases = 0.0

        // Find previous building health reports
        val buildingVisits = db.collection("visits")
            .whereEqualTo("buildingID", buildingIDText.text)

        buildingVisits.get().addOnSuccessListener { result ->
            var numVisits = 0.0
            for (doc in result) {
                val visit : Visit = doc.toObject()
                // only consider visits within last 3 days
                if (withinTimePeriod(visit.endTime!!)) {
                    if(visit.userWasPositive) {
                        posCases++
                    }
                    numVisits++
                }
            }
            posRate = posCases/numVisits
            buildingRiskText.text = getBuildingRisk()
        }

        // Find previous building reports
        val buildingReports = db.collection("buildingReports")
            .whereEqualTo("buildingID", buildingIDText.text)

        buildingReports.get().addOnSuccessListener { result ->
            var numReports= 0.0
            var maskRate = 0.0
            var sanitizer = 0.0
            for (doc in result) {
                val buildingR : BuildingReport = doc.toObject()
                // only consider reports within last 3 days
                if (withinTimePeriod(buildingR.date!!)) {
                    maskRate += buildingR.maskPercentage!!
                    if (buildingR.sanitizer!!) {
                        sanitizer++
                    }
                    numReports++
                }
            }
            avgMaskRate = maskRate/numReports
            avgSanitizer = sanitizer/numReports
            buildingRiskText.text = getBuildingRisk()
        }
        return calculateRisk(posRate, avgMaskRate, avgSanitizer, posCases)
    }

    private fun withinTimePeriod(ts: Timestamp) : Boolean {
        val threeDaysBefore = Calendar.getInstance()
        threeDaysBefore.add(Calendar.DAY_OF_YEAR, -3)
        // if report is within last three days
        if (ts.compareTo(Timestamp(threeDaysBefore.time)) > 0){
            return true
        }
        return false
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
//        string += " There have been " + posCases.toInt() + " covid cases in the past 3 days."
        return string
    }
}

