package com.example.covider.visits

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.covider.LoginActivity
import com.example.covider.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AddVisitActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var spinner: Spinner
    private var buildingList = arrayOf("ACB", "ACC", "ANN", "ASC", "BHE", "BIT", "BKS", "CPA", "DML", "DRC", "ESH",
        "GER", "GFS", "IYH", "JFF", "JHH", "JKP", "KAP", "KDC", "LAW", "LJS", "LRC",
        "LVL", "MCB", "MCC", "MHP", "NCT", "OHE", "PED", "PHE", "RGL", "RHM",
        "RRI", "RTH", "RZC", "SAL", "SCA", "SCB", "SCC", "SGM", "SKS", "SLH",
        "SOS", "STO", "TCC", "THH", "TMC", "UAC", "UGW", "VHE", "WAH", "ZHS")

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_visit)

        spinner = findViewById(R.id.spinner_building)
        if (spinner != null) {
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item, buildingList
            )
            spinner.adapter = adapter
        }
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val building = buildingList[p2]
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        TODO("Not yet implemented")
    }

}