package com.example.covider

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.common.primitives.UnsignedBytes.toInt

import com.example.covider.models.Building
import com.example.covider.models.User

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.ktx.auth

// Move firebase stuff to DatabaseService later
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.toObject


class MapsFragment : Fragment(), GoogleMap.OnInfoWindowClickListener {

    // Firestore
    private val db = Firebase.firestore
    private val auth = Firebase.auth

    // member vars
    private var favoriteBuildings = listOf<String>()

    var buildings = ArrayList<Building>()
    private var buildingMarkers = ArrayList<Marker>() // public so buildings can be accessed elsewhere
    private val priority1MinZoom = 16.5
    private lateinit var listButton: Button

    private var buildingsLoaded = false


    private val callback = OnMapReadyCallback { map ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         */

        // Set initial camera settings
        val uscBounds = LatLngBounds(
            LatLng(34.016779, -118.293452), // southwest corner
            LatLng(34.027559, -118.277238) // northeast corner
        )
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = 0
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(uscBounds, width, height, padding))
        map.setLatLngBoundsForCameraTarget(uscBounds)
        map.setMinZoomPreference(14.0f)

        // Get favorite / recurring class buildings
        getFavoriteBuildings(map) {
            // Draw buildings on map
            getAndDrawBuildings(map)
        }


        // Set listener to zoom so we can draw markers based on zoom
        map.setOnCameraMoveListener { updateMarkersOnZoom(map) }

        // Set listener to marker click
        map.setOnInfoWindowClickListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_maps, container, false)
        listButton = view.findViewById(R.id.button_building_list) // change
        listButton.setOnClickListener {
            val intent = Intent(this.context, BuildingsActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }


    // function to get favorite buildings so we know which ones to mark as yellow on map
    // with callback so we know when we can create the markers
    private fun getFavoriteBuildings(map: GoogleMap, onSuccess: (GoogleMap) -> Unit) {
        // Firebase stuff to get favorite buildings
        val userID = auth.currentUser!!.uid
        val userRef = db.collection("users").document(userID)

        userRef.get().addOnSuccessListener { result ->
            // Store favorite buildings in member var
            val userObj = result.toObject<User>()
            favoriteBuildings = userObj!!.favoriteBuildings

            // Call callback function
            onSuccess(map)
        }
    }

    // -- map interaction -- //
    override fun onInfoWindowClick(marker: Marker) {
        val building = marker.tag as Building

        val intent = Intent(this.context, BuildingInfoActivity::class.java)

        intent.putExtra("buildingID", building.id)
        intent.putExtra("buildingName", building.name)
        intent.putExtra("buildingAddress", building.address)


        startActivity(intent)

        Toast.makeText(
            activity, "${building.id} @ ${building.address}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateMarkersOnZoom(map: GoogleMap) {
        for (marker in buildingMarkers) {
            // priority 2 and 3 always shown. priority 1 only if greater than constant
            val building = marker.tag as Building
            marker.isVisible = when (building.priority) {
                2, 3 -> true
                1 -> map.cameraPosition.zoom >= priority1MinZoom
                else -> false
            }
        }
    }

    // -- helper functions -- //
    private fun getAndDrawBuildings(map: GoogleMap) {
        if (buildingsLoaded) {
            drawBuildingMarkers(map)
        }
        else {
            // pull buildings from Firebase
            db.collection("buildings").get()
                .addOnSuccessListener { result ->
                    // loop through all building docs to create Building object
                    for (doc in result) {
                        val newBuilding = doc.toObject<Building>()

                        // Check if this building is in favoriteBuildings
                        if (favoriteBuildings.contains(newBuilding.id)) {
                            newBuilding.priority = 3
                        }

                        // add to buildings array
                        buildings.add(newBuilding)
                    }

                    buildingsLoaded = true
                    Log.i(TAG(), "Successfully created ${buildings.size} buildings")

                    // Now draw buildings on map
                    drawBuildingMarkers(map)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG(), "Error getting buildings.", exception)
                }
        }


    }

    private fun drawBuildingMarkers(map: GoogleMap) {
        // draw each building that has show as true
        for (b in buildings) {
            if (b.priority == 0) continue // skip buildings that are hidden

            // get icon color based on priority (2 = favorited)
            val hue = when (b.priority) {
                3 -> BitmapDescriptorFactory.HUE_YELLOW
                else -> BitmapDescriptorFactory.HUE_RED
            }
            val customIcon = BitmapDescriptorFactory.defaultMarker(hue)

            // create marker
            val marker = map.addMarker(MarkerOptions().position(GeoPointToLatLng(b.coordinates!!))
                .title(b.name).snippet("Click for more info")
                .icon(customIcon))

            if (marker != null) {
                marker.tag = b // set data tag of marker to corresponding building object
                marker.isVisible = false // set to false initially, so that we can enable a few
                buildingMarkers.add(marker)
            }
        }
        updateMarkersOnZoom(map) // update for the zoom
    }


    private fun GeoPointToLatLng(gp: GeoPoint): LatLng {
        return LatLng(gp.latitude, gp.longitude)
    }
}