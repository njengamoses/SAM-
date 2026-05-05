package com.example.sam.qr

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.sam.databinding.ActivityGenerateQrBinding
import com.example.sam.utils.FirebaseUtils
import com.example.sam.utils.QRUtils
import com.google.firebase.auth.FirebaseAuth

class GenerateQRActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenerateQrBinding
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ensure QR view is visible from start
        binding.ivQr.visibility = View.VISIBLE

        binding.btnCreateSession.setOnClickListener {

            val course = binding.etCourse.text.toString().trim()
            val duration = binding.etDuration.text.toString().toIntOrNull() ?: 10

            if (course.isEmpty()) {
                Toast.makeText(this, "Enter course code", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val lecturerId = auth.currentUser?.uid
            if (lecturerId == null) {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Creating session...", Toast.LENGTH_SHORT).show()

            FirebaseUtils.createSession(
                lecturerId,
                course,
                duration
            ) { ok, sessionId, error ->

                runOnUiThread {

                    Toast.makeText(this, "Callback reached", Toast.LENGTH_SHORT).show()

                    if (ok && sessionId != null) {

                        val payload = "sess:$sessionId"

                        val bitmap = QRUtils.generateQRCode(payload)

                        //  HARD CHECK
                        if (bitmap == null) {
                            Toast.makeText(this, "QR generation FAILED", Toast.LENGTH_LONG).show()
                            return@runOnUiThread
                        }

                        //  FORCE SHOW
                        binding.ivQr.post {
                            binding.ivQr.visibility = android.view.View.VISIBLE
                            binding.ivQr.setImageBitmap(bitmap)
                            binding.ivQr.setBackgroundColor(android.graphics.Color.WHITE)
                        }

                        binding.tvSessionInfo.text =
                            "Course: $course\nSession ID: $sessionId\nDuration: $duration min"

                        Toast.makeText(this, "QR Generated Successfully", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(
                            this,
                            "Error: ${error ?: "Unknown error"}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}