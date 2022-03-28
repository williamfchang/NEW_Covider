package com.example.covider.visits

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.covider.R

/**
 * A fragment representing a list of Items.
 */
class VisitsFragment : Fragment() {
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

        addButton.setOnClickListener {
            val intent = Intent(this.context, AddVisitActivity::class.java)
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