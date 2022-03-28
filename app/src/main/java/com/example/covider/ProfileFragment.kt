package com.example.covider

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.covider.models.User
import com.example.covider.models.CoviderEnums
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private val auth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    private lateinit var linearLayout: LinearLayout

    private lateinit var name: TextView
    private lateinit var userType: TextView
    private lateinit var uscID: TextView
    private lateinit var email: TextView

    private lateinit var courseTable: TableLayout
    private lateinit var hReportTable: TableLayout

    private lateinit var addHealthReportButton: Button
    private lateinit var loginButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        linearLayout = view.findViewById(R.id.profile_linear_layout)

        name = view.findViewById(R.id.text_name)
        userType = view.findViewById(R.id.user_type)
        uscID = view.findViewById(R.id.view_id)
        email = view.findViewById(R.id.view_email)

        courseTable = view.findViewById(R.id.table_courses)
        hReportTable = view.findViewById(R.id.table_health_reports)

        addHealthReportButton = view.findViewById(R.id.button_upload_health_reports)
        loginButton = view.findViewById(R.id.button_profile_login)

        addHealthReportButton.setOnClickListener {
            val intent = Intent(activity, HealthReportActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }
        loginButton.visibility = View.GONE

        val authUser = auth.currentUser
        var user: User
        if (authUser != null){
            val docRef = db.collection("users").document(authUser.uid)
            docRef.get()
                .addOnCompleteListener {  task ->
                    if (task.isSuccessful){
                        val document = task.result
                        user = User(document["name"] as String, document["email"] as String, document["uscID"] as String, CoviderEnums.UserType.valueOf(document["role"] as String))

                        name.text = user.name
                        userType.text = user.role.name
                        uscID.text = user.uscID
                        email.text = user.email
                    }
                    else{
                        onFirestoreGetFailed("Failed to retrieve user info")
                    }
                }
        }
        else{
            linearLayout.visibility = View.GONE
            loginButton.visibility = View.VISIBLE
        }

        return view
    }

    private fun onFirestoreGetFailed(errorMsg: String) {
        Log.e(TAG(), errorMsg)
        Toast.makeText(activity, errorMsg, Toast.LENGTH_LONG).show()
    }
}