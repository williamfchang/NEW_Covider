package com.example.covider.models

import com.google.firebase.Timestamp

data class HealthReport (
    val date: Timestamp? = null,
    val numSymptoms: Int? = null,
    val testedPositive: Boolean? = null,
    val userID: String? = null
)