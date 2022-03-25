package com.example.covider

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import kotlinx.coroutines.*

import io.realm.mongodb.App
import io.realm.mongodb.AppConfiguration
import io.realm.mongodb.AppException
import io.realm.mongodb.Credentials


/* THIS IS ADAPTED FROM MONGODB TUTORIAL: https://www.mongodb.com/docs/realm/tutorial/java-sdk */


lateinit var coviderApp: App

inline fun <reified T> T.TAG(): String = T::class.java.simpleName

/*
* LoginActivity: launched whenever a user isn't already logged in. Allows a user to enter email
* and password credentials to log in to an existing account or create a new account.
*/
class LoginActivity : AppCompatActivity() {
    private lateinit var username: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var createUserButton: Button

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        username = findViewById(R.id.input_username)
        password = findViewById(R.id.input_password)
        loginButton = findViewById(R.id.button_login)
        createUserButton = findViewById(R.id.button_create)

        loginButton.setOnClickListener { login(false) }
        createUserButton.setOnClickListener { login(true) }

        // initialize app
        // TODO: move this to MainActivity once we have login as a popup instead of start page
        coviderApp = App.create(AppConfiguration.Builder(BuildConfig.MONGODB_REALM_APP_ID).build())
    }

    override fun onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true)
    }

    private fun onLoginSuccess() {
        // successful login ends this activity, bringing the user back to the project activity
//        finish()

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
    private fun login(createUser: Boolean) {
        if (!validateCredentials()) {
            onLoginFailed("Invalid username or password")
            return
        }

        // while this operation completes, disable the buttons to login or create a new account
        createUserButton.isEnabled = false
        loginButton.isEnabled = false

        val username = this.username.text.toString()
        val password = this.password.text.toString()

        if (createUser) {
            // register a user using the Realm App we created in the TaskTracker class
            // TODO: change this comment when we move the realm app creation
            try {
                runBlocking {
                    coviderApp.emailPasswordAuth.registerUser(username, password)
                }

                // successfully registered
                Log.i(TAG(), "Successfully registered user.")
                login(false)
            }
            catch (e: AppException) {
                // failure to register
                onLoginFailed("Could not register user.")
                Log.e(TAG(), "Error: $e")
            }
            finally {
                // re-enable the buttons after user registration returns a result
                createUserButton.isEnabled = true
                loginButton.isEnabled = true
            }
        }
        else {
            val creds = Credentials.emailPassword(username, password)

            try {
                runBlocking {
                    coviderApp.login(creds)
                }

                onLoginSuccess()
            } catch (e: AppException) {
                onLoginFailed(e.toString())
            } finally {
                // re-enable the buttons after user login returns a result
                createUserButton.isEnabled = true
                loginButton.isEnabled = true
            }
        }
    }
}
