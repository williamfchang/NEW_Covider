package com.example.covider

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.covider.models.Building
import com.example.covider.visits.AddVisitActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class BuildingsActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private var buildings = HashMap<String, Building>()
    private var buildingsByPriority = ArrayList<String>()

    // firestore
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buildings)

        listView = findViewById(R.id.building_list_view)
        getBuildingInfo()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, emptyArray<String>())
        listView.adapter = adapter
    }

    private fun getBuildingInfo() {
            // pull buildings from Firebase
            db.collection("buildings").get()
            .addOnSuccessListener { result ->
                // loop through all building docs to create Building object
                for (doc in result) {
                    buildings[doc.id] = doc.toObject<Building>()
                }

                Log.i(TAG(), "Successfully created ${buildings.size} buildings")

                // Now draw buildings on map
                insertBuildingsByPriority()

                Log.i(TAG(), buildingsByPriority.toString())
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, buildingsByPriority)
                listView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.w(TAG(), "Error getting buildings.", exception)
            }
    }

    private fun insertBuildingsByPriority () {
        var priority2 = ArrayList<String>()
        var priority1 = ArrayList<String>()
        for ((bcode, b) in buildings) {
            val bId = when (b.id) {
                null -> "N/A"
                else -> b.id
            }
            if (b.priority == 2) {
                priority2.add(bId + " [*]")
            } else if (b.priority == 1) {
                priority1.add(bId)
            }
        }
        buildingsByPriority.addAll(priority2)
        buildingsByPriority.addAll(priority1)

        listView.setOnItemClickListener { adapterView, view, i, l ->
            // TODO: Want to open building info tab first, but do this for testing
            val intent = Intent(this, BuildingInfoActivity::class.java)

            val bCode = buildingsByPriority[i].substring(0, 3)
            val building = buildings[bCode]!!
            intent.putExtra("buildingID", bCode)
            intent.putExtra("buildingName", building.name)
            intent.putExtra("buildingAddress", building.address)


            startActivity(intent)
        }
    }
}