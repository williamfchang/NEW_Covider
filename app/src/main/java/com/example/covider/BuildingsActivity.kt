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
import com.example.covider.models.User
import com.example.covider.visits.AddVisitActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

class BuildingsActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    var buildings = HashMap<String, Building>()
    var buildingsByPriority = ArrayList<String>()

    // firestore
    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private var buildingsLoaded = false // don't want to load buildings multiple times

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buildings)

        listView = findViewById(R.id.building_list_view)
        getBuildingInfo()

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, emptyArray<String>())
        listView.adapter = adapter
    }

    private fun getBuildingInfo() {
        // if buildings are already loaded, just load the page
        if (buildingsLoaded) {
            Log.i(TAG(), "Buildings already loaded!")

            // Get buildings by priority
            insertBuildingsByPriority()

            Log.i(TAG(), buildingsByPriority.toString())
            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, buildingsByPriority)
            listView.adapter = adapter
        }
        else {
            // otherwise, pull buildings from Firebase
            val userRef = db.collection("users").document(auth.currentUser!!.uid)
            userRef.get().addOnSuccessListener { userResult ->
                val courses = userResult.toObject<User>()!!.courses

                // get buildings for courses, then use callback to continue the logic
                getBuildingsForCourses(courses) { favoriteBuildings ->
                    db.collection("buildings").get()
                        .addOnSuccessListener { result ->
                            // loop through all building docs to create Building object
                            for (doc in result) {
                                val newBuilding = doc.toObject<Building>()

                                // Check if this building is in favoriteBuildings
                                if (favoriteBuildings.contains(newBuilding.id)) {
                                    newBuilding.priority = 3
                                }

                                buildings[doc.id] = newBuilding
                            }

                            buildingsLoaded = true
                            Log.i(TAG(), "Successfully created ${buildings.size} buildings")

                            // Get buildings by priority
                            insertBuildingsByPriority()

                            Log.i(TAG(), buildingsByPriority.toString())
                            val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, buildingsByPriority)
                            listView.adapter = adapter
                        }
                        .addOnFailureListener { exception ->
                            Log.w(TAG(), "Error getting buildings.", exception)
                        }
                }
            }



        }
    }

    private fun getBuildingsForCourses(courses: List<String>, callback: (buildings: List<String>) -> Unit) {
        val courseDocsRef = db.collection("courses").whereIn("section", courses)
        courseDocsRef.get().addOnSuccessListener { results ->
            var favoriteBuildings = mutableListOf<String>()

            // go through each document
            for (doc in results) {
                favoriteBuildings.add(doc["buildingID"] as String)
            }

            // now call callback with favoriteBuildings
            callback(favoriteBuildings)
        }
    }


    // make function static
    companion object {
        fun sortBuildings(buildingsMap: HashMap<String, Building>): ArrayList<String> {
            var priority3 = ArrayList<String>()
            var priority2 = ArrayList<String>()
            var priority1 = ArrayList<String>()
            for ((bcode, b) in buildingsMap) {
                val bId = when (b.id) {
                    null -> "N/A"
                    else -> b.id
                }
                val bColonAndName = when (b.name) {
                    null -> ""
                    else -> ": ${b.name}"
                }

                if (b.priority == 3) {
                    priority3.add("$bId [**]$bColonAndName")
                } else if (b.priority == 2) {
                    priority2.add("$bId [*]$bColonAndName")
                } else if (b.priority == 1) {
                    priority1.add("$bId$bColonAndName")
                }
            }

            var out = ArrayList<String>()
            out.addAll(priority3)
            out.addAll(priority2)
            out.addAll(priority1)

            return out
        }
    }

    fun insertBuildingsByPriority () {
        buildingsByPriority = sortBuildings(buildings)

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