package com.example.sam.student

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import com.example.sam.R
import com.example.sam.qr.ScanQRActivity


class StudentDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_student_dashboard)

        val scanQrBtn = findViewById<Button>(R.id.scanQrBtn)
        val historyBtn = findViewById<Button>(R.id.historyBtn)

        scanQrBtn.setOnClickListener {
            startActivity(Intent(this, ScanQRActivity::class.java))
        }

        historyBtn.setOnClickListener {
            startActivity(Intent(this, AttendanceHistoryActivity::class.java))
        }
    }
}