package com.example.sam

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.sam.auth.LoginActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser

        if (currentUser != null) {
            // User already logged in → go to LoginActivity (it will route by role)
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            // No user → go to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}