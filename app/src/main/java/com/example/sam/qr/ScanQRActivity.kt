package com.example.sam.qr

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.sam.databinding.ActivityScanQrBinding
import com.example.sam.utils.FirebaseUtils
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.zxing.integration.android.IntentIntegrator

class ScanQRActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanQrBinding
    private val auth = FirebaseAuth.getInstance()
    private val fusedClient by lazy { LocationServices.getFusedLocationProviderClient(this) }

    private var pendingSessionId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStartScan.setOnClickListener { startScanner() }
    }

    // CAMERA

    private fun startScanner() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }

        val integrator = IntentIntegrator(this)
        integrator.setPrompt("Scan session QR")
        integrator.setBeepEnabled(true)
        integrator.setOrientationLocked(true)
        integrator.initiateScan()
    }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startScanner()
            else Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }

    // SCAN RESULT

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (result != null && result.contents != null) {
            handleScannedPayload(result.contents)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // HANDLE QR

    private fun handleScannedPayload(payload: String) {

        if (!payload.startsWith("sess:")) {
            Toast.makeText(this, "Invalid QR", Toast.LENGTH_SHORT).show()
            return
        }

        val sessionId = payload.substringAfter("sess:")

        FirebaseUtils.fetchSession(sessionId) { session, error ->

            if (session == null) {
                Toast.makeText(this, error ?: "Session not found", Toast.LENGTH_SHORT).show()
                return@fetchSession
            }

            val startTime = session["startTime"] as? Long
            val endTime = session["endTime"] as? Long

            if (startTime == null || endTime == null) {
                Toast.makeText(this, "Invalid session data", Toast.LENGTH_SHORT).show()
                return@fetchSession
            }

            val now = System.currentTimeMillis()

            if (now < startTime) {
                Toast.makeText(this, "Session not started yet", Toast.LENGTH_SHORT).show()
                return@fetchSession
            }

            if (now > endTime) {
                Toast.makeText(this, "Session expired", Toast.LENGTH_SHORT).show()
                return@fetchSession
            }

            val lat = session["latitude"] as? Double
            val lon = session["longitude"] as? Double

            if (lat != null && lon != null) {
                pendingSessionId = sessionId
                checkLocationAndValidate(lat, lon)
            } else {
                markAttendance(sessionId, null, null)
            }
        }
    }

    // LOCATION

    @SuppressLint("MissingPermission")
    private fun checkLocationAndValidate(targetLat: Double, targetLon: Double) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        fusedClient.lastLocation
            .addOnSuccessListener { location: Location? ->

                if (location == null) {
                    Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val devLat = location.latitude
                val devLon = location.longitude

                val results = FloatArray(1)

                Location.distanceBetween(
                    targetLat,
                    targetLon,
                    devLat,
                    devLon,
                    results
                )

                val distance = results[0]

                if (distance > 50f) {
                    Toast.makeText(
                        this,
                        "You are too far from session location",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@addOnSuccessListener
                }

                pendingSessionId?.let {
                    markAttendance(it, devLat, devLon)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not get location", Toast.LENGTH_SHORT).show()
            }
    }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                pendingSessionId?.let {
                    Toast.makeText(this, "Location granted. Continuing…", Toast.LENGTH_SHORT).show()
                    handleScannedPayload("sess:$it")
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    // ATTENDANCE

    private fun markAttendance(sessionId: String, lat: Double?, lon: Double?) {

        val studentId = auth.currentUser?.uid
            ?: run {
                Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show()
                return
            }

        FirebaseUtils.markAttendance(sessionId, studentId, lat, lon) { ok, info ->
            if (ok) {
                Toast.makeText(this, "Attendance recorded", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Not recorded: $info", Toast.LENGTH_LONG).show()
            }
        }
    }
}