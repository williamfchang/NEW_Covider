package com.example.covider.services

import android.util.Log
import android.widget.Toast
import com.example.covider.TAG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthService {
    private var auth: FirebaseAuth = Firebase.auth
}