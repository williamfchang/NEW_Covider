package com.example.covider

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*


class MapsFragment : Fragment(), GoogleMap.OnInfoWindowClickListener {

    // member vars
    private val buildings = arrayOf(
        Building("Tutor Campus Center", LatLng(34.020473494587414, -118.2863610986055)),
        Building("Grace Ford Salvatori Hall", LatLng(34.02145610419585, -118.2879972637911),
            minZoom = 17.0f),
        Building("Leavey Library", LatLng(34.02178321140542, -118.28284243164403),
            isFavorite = true, minZoom = 16.0f)
    )
    private var buildingMarkers = ArrayList<Marker>()


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

        // Draw markers at each building
        for (b in buildings) {
            // get icon color based on favorited
            val hue = if (b.isFavorite) BitmapDescriptorFactory.HUE_YELLOW
                        else BitmapDescriptorFactory.HUE_RED
            val customIcon = BitmapDescriptorFactory.defaultMarker(hue)

            // create marker
            val marker = map.addMarker(MarkerOptions().position(b.coords)
                .title(b.name).snippet("Click for more info")
                .icon(customIcon))

            if (marker != null) {
                marker.tag = b // set data tag of marker to corresponding building object
                buildingMarkers.add(marker)
            }
        }
        updateMarkersOnZoom(map) // an initial update for current zoom

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

    override fun onInfoWindowClick(marker: Marker) {
        Toast.makeText(
            activity, "info window of ${marker.title} clicked",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateMarkersOnZoom(map: GoogleMap) {
        for (marker in buildingMarkers) {
            marker.isVisible = map.cameraPosition.zoom >= (marker.tag as Building).minZoom
        }
    }
}


class Building(val name: String, val coords: LatLng, val isFavorite: Boolean=false, var minZoom: Float=14.0f)