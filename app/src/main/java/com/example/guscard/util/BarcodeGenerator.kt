package com.example.guscard.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter

object BarcodeGenerator {

    fun generate(
        content: String,
        formatName: String,
        width: Int = 1000,
        height: Int = 400,
        invert: Boolean = false
    ): Bitmap? {
        return try {
            val format = try {
                BarcodeFormat.valueOf(formatName)
            } catch (e: Exception) {
                BarcodeFormat.CODE_128
            }
            val matrix = MultiFormatWriter().encode(content, format, width, height)
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val on = if (invert) Color.WHITE else Color.BLACK
            val off = if (invert) Color.BLACK else Color.WHITE
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (matrix[x, y]) on else off)
                }
            }
            bmp
        } catch (e: Exception) {
            null
        }
    }
    // Распознавание штрих-кода с картинки (Bitmap)
    fun decodeFromBitmap(bitmap: android.graphics.Bitmap): String? {
        val intArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        val source = com.google.zxing.RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
        val binaryBitmap = com.google.zxing.BinaryBitmap(com.google.zxing.common.HybridBinarizer(source))
        val reader = com.google.zxing.MultiFormatReader()
        return try {
            val result = reader.decode(binaryBitmap)
            result.text
        } catch (e: Exception) {
            null
        }
    }
}