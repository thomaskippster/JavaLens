package com.javalens.app.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.javalens.app.domain.logic.CodeStitcher
import com.javalens.app.domain.logic.FileSplitter
import com.javalens.app.domain.video.VideoCodeExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class VideoImportViewModel(
    private val videoExtractor: VideoCodeExtractor,
    private val stitcher: CodeStitcher = CodeStitcher(),
    private val fileSplitter: FileSplitter = FileSplitter()
) : ViewModel() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val _isParsing = MutableStateFlow(false)
    val isParsing = _isParsing.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    private val _extractedCode = MutableStateFlow("")
    val extractedCode = _extractedCode.asStateFlow()

    fun parseVideo(videoUri: Uri) {
        viewModelScope.launch {
            _isParsing.value = true
            _progress.value = 0f
            _extractedCode.value = ""
            
            // Annahme: Wir kennen die ungefähre Frame-Anzahl oder berechnen sie
            var processedFrames = 0
            
            videoExtractor.extractFrames(videoUri).collect { bitmap ->
                processedFrames++
                // Beispielhafte Fortschrittsberechnung (muss an Video-Dauer angepasst werden)
                _progress.value = (processedFrames % 100) / 100f 
                
                val image = InputImage.fromBitmap(bitmap, 0)
                try {
                    val visionText = recognizer.process(image).await()
                    if (visionText.text.isNotBlank()) {
                        val stitched = stitcher.stitch(_extractedCode.value, visionText.text)
                        _extractedCode.value = stitched
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            _progress.value = 1f
            _isParsing.value = false
        }
    }
}
