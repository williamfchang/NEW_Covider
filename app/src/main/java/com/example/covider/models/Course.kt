package com.example.covider.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class Course(
    val section: String = "",
    val title: String = "",
    val buildingID: String = "",
    val days: List<Boolean> = listOf<Boolean>(),
    var startTime: Timestamp? = null,
    var endTime: Timestamp? = null,
    val instructors:MutableList<IdAndName> = mutableListOf<IdAndName>(),
    val students:MutableList<IdAndName> = mutableListOf<IdAndName>(),
    var mode: CoviderEnums.ClassMode = CoviderEnums.ClassMode.INPERSON,

    )