package com.example.covider

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


/* THIS IS ADAPTED FROM MONGODB TUTORIAL: https://www.mongodb.com/docs/realm/tutorial/java-sdk */

inline fun <reified T> T.TAG(): String = T::class.java.simpleName

/*
* LoginActivity: launched whenever a user isn't already logged in. Allows a user to enter email
* and password credentials to log in to an existing account or create a new account.
*/
class LoginActivity : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var gotoCreateAccountButton: Button

    private lateinit var auth: FirebaseAuth

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        username = findViewById(R.id.input_email)
        password = findViewById(R.id.input_password)
        loginButton = findViewById(R.id.button_create)
        gotoCreateAccountButton = findViewById(R.id.button_login)

        loginButton.setOnClickListener { login() }
        gotoCreateAccountButton.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }

        // Initialize Firebase Auth
        auth = Firebase.auth
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            onLoginSuccess()
        }
    }

    override fun onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true)
    }

    private fun onLoginSuccess() {
        // successful login ends this activity, bringing the user back to the project activity
        // finish()

        // temporarily go to main activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    private fun onLoginFailed(errorMsg: String) {
        Log.e(TAG(), errorMsg)
        Toast.makeText(baseContext, errorMsg, Toast.LENGTH_LONG).show()
    }

    private fun validateCredentials(): Boolean = when {
        // zero-length usernames and passwords are not valid (or secure), so prevent users from creating accounts with those client-side.
        username.text.toString().isEmpty() -> false
        password.text.toString().isEmpty() -> false
        else -> true
    }

    // handle user authentication (login) and account creation
    private fun login() {
        if (!validateCredentials()) {
            onLoginFailed("Invalid username or password")
            return
        }

        // while this operation completes, disable the buttons to login or create a new account
        gotoCreateAccountButton.isEnabled = false
        loginButton.isEnabled = false

        val username = this.username.text.toString()
        val password = this.password.text.toString()

        auth.signInWithEmailAndPassword(username, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG(), "signInWithEmail:success")
                    onLoginSuccess()
                } else {
                    // If sign in fails, display a message to the user.
                    onLoginFailed("Login failed")
                    gotoCreateAccountButton.isEnabled = true
                    loginButton.isEnabled = true
                }
            }
    }
}
