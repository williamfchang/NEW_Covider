package com.example.covider.models

import com.google.firebase.Timestamp

data class BuildingReport (
    val buildingID: String? = null,
    val date: Timestamp? = null,
    val sanitizer: Boolean? = null,
    val maskPercentage: Double? = null,
    val userID: String? = null
)