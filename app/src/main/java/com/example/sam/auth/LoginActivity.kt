package com.example.sam.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sam.R
import com.example.sam.dashboard.LecturerDashboardActivity
import com.example.sam.student.StudentDashboardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val emailInput = findViewById<EditText>(R.id.emailInput)
        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val registerBtn = findViewById<Button>(R.id.registerBtn)

        loginBtn.setOnClickListener {

            val email = emailInput.text.toString().trim().lowercase()
            val password = passwordInput.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter both email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginBtn.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    val user = auth.currentUser
                    if (user == null) {
                        Toast.makeText(this, "User not found after login", Toast.LENGTH_SHORT).show()
                        loginBtn.isEnabled = true
                        return@addOnSuccessListener
                    }

                    val userId = user.uid

                    db.collection("users").document(userId)
                        .get()
                        .addOnSuccessListener { document ->

                            if (!document.exists()) {
                                Toast.makeText(
                                    this,
                                    "User data not found. Please register again.",
                                    Toast.LENGTH_LONG
                                ).show()

                                //  Prevent broken state
                                auth.signOut()
                                loginBtn.isEnabled = true
                                return@addOnSuccessListener
                            }

                            val role = document.getString("role")

                            if (role.isNullOrEmpty()) {
                                Toast.makeText(
                                    this,
                                    "User role missing. Contact admin.",
                                    Toast.LENGTH_LONG
                                ).show()

                                auth.signOut()
                                loginBtn.isEnabled = true
                                return@addOnSuccessListener
                            }

                            when (role) {
                                "Student" -> {
                                    startActivity(Intent(this, StudentDashboardActivity::class.java))
                                    finish()
                                }
                                "Lecturer" -> {
                                    startActivity(Intent(this, LecturerDashboardActivity::class.java))
                                    finish()
                                }
                                else -> {
                                    Toast.makeText(
                                        this,
                                        "Invalid role: $role",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    auth.signOut()
                                    loginBtn.isEnabled = true
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Failed to fetch user role: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()

                            loginBtn.isEnabled = true
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Login failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    loginBtn.isEnabled = true
                }
        }

        registerBtn.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}