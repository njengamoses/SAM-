package com.example.sam.utils

import android.graphics.Bitmap
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.google.zxing.BarcodeFormat

object QRUtils {

    fun generateQRCode(text: String): Bitmap {
        val encoder = BarcodeEncoder()
        return try {
            encoder.encodeBitmap(text, BarcodeFormat.QR_CODE, 800, 800)
        } catch (e: Exception) {
            throw RuntimeException("QR generation failed: ${e.message}")
        }
    }
}