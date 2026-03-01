package com.example.javalens.service

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class FileSplitter {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val classRegex = Regex("(?:public\\s+)?class\\s+(\\w+)")

    fun processImage(bitmap: Bitmap, onResult: (String, String?) -> Unit) {
        val image = InputImage.fromBitmap(bitmap, 0)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val fullText = visionText.text
                val className = detectClassName(fullText)
                onResult(fullText, className)
            }
    }

    fun detectClassName(code: String): String? {
        return classRegex.find(code)?.groupValues?.get(1)
    }

    fun generateFileName(className: String?): String {
        return className?.let { "$it.java" } ?: "UntitledScan_${System.currentTimeMillis()}.java"
    }
}
