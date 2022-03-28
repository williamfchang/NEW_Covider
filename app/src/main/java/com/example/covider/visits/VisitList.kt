package com.example.covider.visits

import com.example.covider.models.Visit
import java.util.ArrayList

object VisitList {

    // TODO: need some way to order this list
    val visits: MutableList<Visit> = ArrayList()

    fun addVisit(visit: Visit) {
        visits.add(visit)
    }
}