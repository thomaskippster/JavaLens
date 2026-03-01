package com.javalens.app.ui.components

import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.javalens.app.domain.ocr.LiveTextAnalyzer
import java.util.concurrent.Executors

@Composable
fun CameraPreviewView(
    modifier: Modifier = Modifier,
    isScanningActive: Boolean,
    onTextExtracted: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Wir nutzen einen Single-Thread Executor für die ML Kit Analyse, 
    // um die UI nicht zu blockieren.
    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                // 1. Preview Use Case (Was der User auf dem Screen sieht)
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                // 2. ImageAnalysis Use Case (Was die OCR "sieht")
                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Verhindert Stau!
                    .build()
                    .also {
                        it.setAnalyzer(
                            analysisExecutor,
                            LiveTextAnalyzer { extractedText ->
                                // Nur verarbeiten, wenn der User den "Body Button" gedrückt hat
                                if (isScanningActive) {
                                    onTextExtracted(extractedText)
                                }
                            }
                        )
                    }

                // Wir nutzen die Rückkamera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    // Alten Kram unbinden und neu starten
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", exc)
                }
            }, ContextCompat.getMainExecutor(ctx))
            
            previewView
        }
    )
}
