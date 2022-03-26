package com.example.covider.visits

import java.util.ArrayList

object VisitList {

    // TODO: need some way to order this list
    val visits: MutableList<Visit> = ArrayList()
    private val count = 18

    init {
        // Add some sample items
        for (i in 1..count) {
            visits.add(createPlaceholderVisit(i))
        }
    }

    private fun createPlaceholderVisit(position: Int): Visit {
        return Visit(position, position+10, "building name")
    }
}

class Visit {
    private var startTime = 0 //
    private var endTime = 0
    private var buildingID = ""

    constructor(startTime: Int, endTime: Int, buildingID: String) {
        this.startTime = startTime
        this.endTime = endTime
        this.buildingID = buildingID
    }

    fun getStartTime(): Int {
        return startTime
    }
    fun getEndTime(): Int {
        return endTime
    }
    fun getBuilding(): String {
        return buildingID
    }
}