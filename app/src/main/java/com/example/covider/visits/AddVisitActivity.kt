package com.example.covider.visits

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.covider.BuildingsActivity
import com.example.covider.LoginActivity
import com.example.covider.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AddVisitActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var building: String
    private lateinit var spinner: Spinner
    private lateinit var addButton: Button

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_visit)

        spinner = findViewById(R.id.spinner_building)
        if (spinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, resources.getStringArray(R.array.Buildings)
            )
            spinner.adapter = adapter
        }
        var buildingID: Int = intent.getIntExtra("Building Index", 0)
        spinner.setSelection(buildingID)

        // TODO: Change
        addButton = findViewById(R.id.button_add_visit)
        addButton.setOnClickListener {
//            // actually add the visit
//            val intent = Intent(this, BuildingsActivity::class.java)
//            startActivity(intent)
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        building = resources.getStringArray(R.array.Buildings)[p2]
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}