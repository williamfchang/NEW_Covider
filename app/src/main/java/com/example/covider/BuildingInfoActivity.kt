package com.example.covider

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.example.covider.models.Building
import com.example.covider.visits.AddVisitActivity

class BuildingInfoActivity : AppCompatActivity() {
    private lateinit var addButton: Button
    private lateinit var buildingCode: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_info)

        var buildingID: Int = intent.getIntExtra("Building Index", 0)

        addButton = findViewById(R.id.button_add_building_visit)
        addButton.setOnClickListener {
            val intent = Intent(this, AddVisitActivity::class.java)
            intent.putExtra("Building Index", buildingID)
            startActivity(intent)
        }
        buildingCode = findViewById(R.id.building_code_label)
        val buildings = resources.getStringArray(R.array.Buildings)
        buildingCode.text = buildings[buildingID]
    }
}