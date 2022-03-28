package com.example.covider.models

import com.google.android.gms.maps.model.LatLng

class Building(val id: String,
               val name: String,
               val coordinates: LatLng,
               val address: String = "N/A",
               val priority: Int = 0)

// var minZoom: Float=14.0f


/* Priority is defined as follows:
   0 -> not shown
   1 -> common building (shown at certain zoom)
   2 -> most common buildings (shown at any zoom)
   3 -> favorited (shown at any zoom, yellow marker)
 */