package com.example.sam.utils

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseUtils {

    val firestore = FirebaseFirestore.getInstance()

    //  CREATE SESSION
    fun createSession(
        lecturerId: String,
        course: String,
        duration: Int,
        callback: (Boolean, String?, String?) -> Unit
    ) {
        val sessionId = firestore.collection("sessions").document().id

        val startTime = System.currentTimeMillis()
        val endTime = startTime + (duration * 60 * 1000)

        val sessionData = hashMapOf(
            "sessionId" to sessionId,
            "lecturerId" to lecturerId,
            "course" to course,
            "startTime" to startTime,
            "endTime" to endTime,
            "status" to "active",
            "latitude" to null,
            "longitude" to null
        )

        firestore.collection("sessions")
            .document(sessionId)
            .set(sessionData)
            .addOnSuccessListener {
                callback(true, sessionId, null)
            }
            .addOnFailureListener {
                callback(false, null, it.message)
            }
    }

    //  FETCH SESSION (STRONGER)
    fun fetchSession(
        sessionId: String,
        callback: (Map<String, Any>?, String?) -> Unit
    ) {
        firestore.collection("sessions")
            .document(sessionId)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    callback(null, "Session not found")
                    return@addOnSuccessListener
                }

                val data = doc.data
                val endTime = data?.get("endTime") as? Long
                val status = data?.get("status") as? String

                //  VALIDATION
                if (status != "active") {
                    callback(null, "Session is closed")
                    return@addOnSuccessListener
                }

                if (endTime != null && System.currentTimeMillis() > endTime) {
                    callback(null, "Session expired")
                    return@addOnSuccessListener
                }

                callback(data, null)
            }
            .addOnFailureListener {
                callback(null, it.message)
            }
    }

    //  MARK ATTENDANCE (WITH VALIDATION)
    fun markAttendance(
        sessionId: String,
        studentId: String,
        lat: Double?,
        lon: Double?,
        callback: (Boolean, String?) -> Unit
    ) {

        //  FIRST CHECK SESSION
        fetchSession(sessionId) { session, error ->

            if (session == null) {
                callback(false, error ?: "Invalid session")
                return@fetchSession
            }

            val attendanceId = firestore.collection("attendance").document().id

            val data = hashMapOf(
                "attendanceId" to attendanceId,
                "sessionId" to sessionId,
                "studentId" to studentId,
                "timestamp" to System.currentTimeMillis(),
                "latitude" to lat,
                "longitude" to lon
            )

            firestore.collection("attendance")
                .document(attendanceId)
                .set(data)
                .addOnSuccessListener {
                    callback(true, null)
                }
                .addOnFailureListener {
                    callback(false, it.message)
                }
        }
    }

    //  END SESSION (PROPER CLOSE)
    fun endSession(
        sessionId: String,
        callback: (Boolean, String?) -> Unit
    ) {
        firestore.collection("sessions")
            .document(sessionId)
            .update(
                mapOf(
                    "endTime" to System.currentTimeMillis(),
                    "status" to "closed"
                )
            )
            .addOnSuccessListener {
                callback(true, null)
            }
            .addOnFailureListener {
                callback(false, it.message)
            }
    }
}