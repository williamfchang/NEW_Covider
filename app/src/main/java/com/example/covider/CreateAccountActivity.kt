package com.example.covider

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.covider.models.Instructor
import com.example.covider.models.Student
import com.example.covider.models.User
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore


class CreateAccountActivity : AppCompatActivity() {
    private lateinit var name: EditText
    private lateinit var uscID: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var gotoLoginButton: Button
    private lateinit var createUserButton: Button
    private lateinit var instructorSwitch: Switch


    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var userdata : User
    private var isInstructor : Boolean = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        name = findViewById(R.id.input_name)
        uscID = findViewById(R.id.input_usc_id)
        email = findViewById(R.id.input_email)
        password = findViewById(R.id.input_password)
        gotoLoginButton = findViewById(R.id.button_login)
        createUserButton = findViewById(R.id.button_create)
        instructorSwitch = findViewById(R.id.switch_instructor)

        createUserButton.setOnClickListener { createAccount() }
        gotoLoginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        instructorSwitch.setOnCheckedChangeListener {_, isChecked ->
            isInstructor = isChecked
        }

        // Initialize Firebase Auth
        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

    }

    private fun onCreateAccountSuccess() {
        // successful login ends this activity, bringing the user back to the project activity
        // finish()

        // temporarily go to main activity
        Toast.makeText(baseContext, "Successfully created account!", Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun onCreateAccountFailed(errorMsg: String) {
        Log.e(TAG(), errorMsg)
        Toast.makeText(baseContext, errorMsg, Toast.LENGTH_LONG).show()
    }

    private fun validateCredentials(): Boolean = when {
        // zero-length usernames and passwords are not valid (or secure), so prevent users from creating accounts with those client-side.
        email.text.toString().isEmpty() -> false
        password.text.toString().isEmpty() -> false
        uscID.text.toString().isEmpty() -> false
        name.text.toString().isEmpty() -> false
        else -> true
    }

    private fun validateInstructor(id:String): Boolean{
        // check instructor id with usc database
        // ensure there isn't already
        Log.e(TAG(), id)
        return true
    }

    private fun createAccount() {
        if (!validateCredentials()) {
            onCreateAccountFailed("Please fill out all sections")
            return
        }

        val name = this.name.text.toString()
        val email = this.email.text.toString()
        val password = this.password.text.toString()
        val uscID = this.uscID.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG(), "createUserWithEmail:success")

                    val user = auth.currentUser

                    userdata =  if (isInstructor && validateInstructor(uscID)) {
                                    Instructor(name, email, uscID)
                                } else {
                                    Student(name, email, uscID)
                                }

                    db.collection("users").document(user!!.uid).set(userdata)

                    onCreateAccountSuccess()

                } else {
                    // If sign in fails, display a message to the user.
                    onCreateAccountFailed("Account creation failed")

                    createUserButton.isEnabled = true
                    gotoLoginButton.isEnabled = true
                }
            }
    }
}