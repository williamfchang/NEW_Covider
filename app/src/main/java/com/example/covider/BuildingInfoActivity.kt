package com.example.covider

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import com.example.covider.models.Building
import com.example.covider.visits.AddVisitActivity
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BuildingInfoActivity : AppCompatActivity() {
    private lateinit var addButton: Button
    private lateinit var buildingIDText: TextView
    private lateinit var buildingNameText: TextView
    private lateinit var buildingAddressText: TextView

    // firestore
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_building_info)

        val buildingID = intent.getStringExtra("buildingID")
        val buildingName= intent.getStringExtra("buildingName")
        val buildingAddress= intent.getStringExtra("buildingAddress")

        // TODO change how buildingID is being sent in
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
    }

    private fun getBuildingName() {
        }

//    private fun getBuildings() : ArrayList<Building> {
//        val buildings = ArrayList<Building>()
//        // pull buildings from Firebase
//        db.collection("buildings").get()
//            .addOnSuccessListener { result ->
//
//                // loop through all buildings
//                for (doc in result) {
//                    // get data fields
//                    val data = doc.data
//
//                    val name = data["name"] as String
//                    val coordAsGP = data["coordinates"] as GeoPoint
//                    val coords = LatLng(coordAsGP.latitude, coordAsGP.longitude)
//                    val address = when {
//                        data.containsKey("address") -> data["address"] as String
//                        else -> "N/A"
//                    }
//                    val priority = (data["priority"] as Long).toInt()
//
//                    // create new building for array
//                    buildings.add(Building(doc.id, name, coords, address, priority))
//                }
//
//                Log.i(TAG(), "Successfully created ${buildings.size} buildings")
//
//            }
//            .addOnFailureListener { exception ->
//                Log.w(TAG(), "Error getting buildings.", exception)
//            }
//        return buildings
//    }
}