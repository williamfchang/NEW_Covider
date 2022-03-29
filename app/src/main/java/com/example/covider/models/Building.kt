package com.example.covider.models

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import java.io.Serializable

data class Building (
    @DocumentId val id: String? = null,
    val name: String? = null,
    val coordinates: GeoPoint? = null,
    val address: String? = null,
    val priority: Int = 0,
    val risk: Int = 1000
) : Serializable


/* Priority is defined as follows:
   0 -> not shown
   1 -> common building (shown at certain zoom)
   2 -> most common buildings (shown at any zoom)
   3 -> favorited (shown at any zoom, yellow marker)
 */