package com.example.sam.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sam.databinding.ActivityLecturerDashboardBinding
import com.example.sam.qr.GenerateQRActivity
import com.google.firebase.auth.FirebaseAuth


class LecturerDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLecturerDashboardBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLecturerDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = auth.currentUser
        binding.tvLecturerWelcome.text = "Welcome, ${user?.email ?: "Lecturer"}"

        binding.btnGenerateQr.setOnClickListener {
            // open generate screen
            startActivity(Intent(this, GenerateQRActivity::class.java))
        }

        binding.btnViewSessions.setOnClickListener {
            Toast.makeText(this, "View Sessions - not implemented yet", Toast.LENGTH_SHORT).show()
            // Optionally implement list of sessions and details
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            finish()
        }
    }
}
