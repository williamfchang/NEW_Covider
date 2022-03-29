package com.example.covider.visits

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Button
import com.example.covider.BuildingsActivity
import com.example.covider.R
import com.example.covider.TAG
import com.example.covider.models.Visit
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.util.*

/**
 * A fragment representing a list of Items.
 */
class VisitsFragment : Fragment() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    private lateinit var addButton: Button
    private lateinit var listView: RecyclerView
    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_visits, container, false)

        listView = view.findViewById(R.id.visit_list)
        addButton = view.findViewById(R.id.button_add_visit_page)

        // Set the adapter
        if (listView is RecyclerView) {
            with(listView) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                adapter = VisitRecyclerViewAdapter(VisitList.visits)
            }
        }

        // -- Load visits from Firebase -- //
        // get time from a week back
        val weekBefore = Calendar.getInstance()
        weekBefore.add(Calendar.DAY_OF_YEAR, -7)

        Log.i(TAG(), "A week before: ${weekBefore.time}")

        // get current user
        val uid = auth.currentUser!!.uid

        // Add visits from the past week to our VisitList
        val recentVisitsQuery = db.collection("visits")
            .whereGreaterThanOrEqualTo("endTime", weekBefore.time)
            .whereEqualTo("userID", uid)

        recentVisitsQuery.get()
            .addOnSuccessListener { result ->
                // clear visit list first
                VisitList.clearVisits()

                // fill in visit list
                for (doc in result) {
                    VisitList.addVisit(doc.toObject())
                }

                listView.adapter!!.notifyDataSetChanged()
                Log.i(TAG(), "retrieved ${VisitList.visits.size} visits")
            }
            .addOnFailureListener { it ->
                Log.w(TAG(), "Error retrieving recent visits of user", it)
            }



        // Logic for add visit button
        addButton = view.findViewById(R.id.button_add_visit_page)
        addButton.setOnClickListener {
            val intent = Intent(this.context, AddVisitActivity::class.java)
            intent.putExtra("buildingID", "ACB")
            startActivity(intent)
        }
        return view
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
                VisitsFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}