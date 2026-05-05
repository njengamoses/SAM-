package com.example.sam.auth

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.sam.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val fullName = findViewById<EditText>(R.id.fullName)
        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val roleSpinner = findViewById<Spinner>(R.id.roleSpinner)
        val registerBtn = findViewById<Button>(R.id.registerBtn)
        val loginLink = findViewById<TextView>(R.id.loginLink)

        registerBtn.setOnClickListener {

            val name = fullName.text.toString().trim()
            val userEmail = email.text.toString().trim().lowercase()
            val userPass = password.text.toString().trim()
            val role = roleSpinner.selectedItem.toString()

            if (name.isEmpty() || userEmail.isEmpty() || userPass.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (userPass.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            registerBtn.isEnabled = false

            auth.createUserWithEmailAndPassword(userEmail, userPass)
                .addOnSuccessListener {

                    val uid = auth.currentUser?.uid

                    if (uid == null) {
                        Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show()
                        registerBtn.isEnabled = true
                        return@addOnSuccessListener
                    }

                    val user = hashMapOf(
                        "name" to name,
                        "email" to userEmail,
                        "role" to role
                    )

                    db.collection("users").document(uid)
                        .set(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show()

                            // Move to login
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->

                            auth.currentUser?.delete()

                            Toast.makeText(
                                this,
                                "Failed to save user data: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()

                            registerBtn.isEnabled = true
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Registration failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()

                    registerBtn.isEnabled = true
                }
        }

        loginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}