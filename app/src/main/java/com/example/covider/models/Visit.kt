package com.example.covider.models

import com.google.firebase.Timestamp
import java.sql.Time

data class Visit(
    var startTime: Timestamp? = null,
    var endTime: Timestamp? = null,
    var buildingID: String? = null,
    var userID: String? = null,
    var userWasPositive: Boolean? = null
)