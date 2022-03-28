package com.example.covider

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import com.example.covider.visits.AddVisitActivity

class BuildingsActivity : AppCompatActivity() {

    private lateinit var listView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buildings)

        listView = findViewById(R.id.building_list_view)
        val buildings = resources.getStringArray(R.array.Buildings)
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, buildings)
        listView.adapter = adapter
        listView.setOnItemClickListener { adapterView, view, i, l ->
            // TODO: Want to open building info tab first, but do this for testing
            val intent = Intent(this, AddVisitActivity::class.java)
            intent.putExtra("Building Index", i)
            startActivity(intent)
        }
    }
}