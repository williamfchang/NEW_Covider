package com.example.covider.visits

import com.example.covider.models.Visit
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