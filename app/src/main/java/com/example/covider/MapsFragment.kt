package com.example.covider

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.common.primitives.UnsignedBytes.toInt

import com.example.covider.models.Building

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

// Move firebase stuff to DatabaseService later
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.GeoPoint


class MapsFragment : Fragment(), GoogleMap.OnInfoWindowClickListener {

    // Firestore
    private val db = Firebase.firestore

    // member vars
    private var buildingMarkers = ArrayList<Marker>()
    private val priority1MinZoom = 16.5

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

        // Draw buildings on map
        getAndDrawBuildings(map)

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
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    // -- map interaction -- //
    override fun onInfoWindowClick(marker: Marker) {
        val building = marker.tag as Building

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
        val buildings = ArrayList<Building>()

        // pull buildings from Firebase
        db.collection("buildings").get()
            .addOnSuccessListener { result ->

                // loop through all buildings
                for (doc in result) {
                    // get data fields
                    val data = doc.data

                    val name = data["name"] as String
                    val coordAsGP = data["coordinates"] as GeoPoint
                    val coords = LatLng(coordAsGP.latitude, coordAsGP.longitude)
                    val address = when {
                        data.containsKey("address") -> data["address"] as String
                        else -> "N/A"
                    }
                    val priority = (data["priority"] as Long).toInt()

                    // create new building for array
                    buildings.add(Building(doc.id, name, coords, address, priority))
                }

                Log.i(TAG(), "Successfully created ${buildings.size} buildings")

                // Now draw buildings on map
                drawBuildingMarkers(map, buildings.toTypedArray())
            }
            .addOnFailureListener { exception ->
                Log.w(TAG(), "Error getting buildings.", exception)
            }
    }

    private fun drawBuildingMarkers(map: GoogleMap, buildings: Array<Building>) {
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
            val marker = map.addMarker(MarkerOptions().position(b.coordinates)
                .title(b.name).snippet("Click for more info")
                .icon(customIcon))

            if (marker != null) {
                marker.tag = b // set data tag of marker to corresponding building object
                buildingMarkers.add(marker)

//                Log.i(TAG(), "Coordinates for ${b.id}: ${b.coordinates}")
            }
        }
        updateMarkersOnZoom(map) // update for the zoom
    }
}