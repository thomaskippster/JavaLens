package com.javalens.app.viewmodel

import android.content.Context
import android.net.Uri
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.javalens.app.data.SnippetEntity
import com.javalens.app.domain.logic.CodeStitcher
import com.javalens.app.domain.logic.FileSplitter
import com.javalens.app.domain.model.Resource
import com.javalens.app.domain.repository.SnippetRepository
import com.javalens.app.domain.video.ExtractionEvent
import com.javalens.app.domain.video.VideoCodeExtractor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber

class ScannerViewModel(
    private val repository: SnippetRepository,
    private val videoExtractor: VideoCodeExtractor
) : ViewModel() {

    private val codeStitcher = CodeStitcher()
    private val fileSplitter = FileSplitter()
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isParsing = MutableStateFlow(false)
    val isParsing: StateFlow<Boolean> = _isParsing.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    val progress: StateFlow<Float> = _progress.asStateFlow()

    private val _currentScannedCode = MutableStateFlow("")
    val currentScannedCode: StateFlow<String> = _currentScannedCode.asStateFlow()
    
    private val _detectedFileName = MutableStateFlow("Ready to scan")
    val detectedFileName: StateFlow<String> = _detectedFileName.asStateFlow()

    private val _aiProcessState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val aiProcessState: StateFlow<Resource<String>> = _aiProcessState.asStateFlow()

    private val _saveResult = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val saveResult: StateFlow<Resource<Unit>> = _saveResult.asStateFlow()

    private var activeRecording: Recording? = null

    fun startRecording(context: Context, videoCapture: VideoCapture<Recorder>) {
        val fileName = "scan_${System.currentTimeMillis()}.mp4"
        val outputOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver,
            android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(android.content.ContentValues().apply {
            put(android.provider.MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(android.provider.MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        }).build()

        activeRecording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        _isRecording.value = true
                        _currentScannedCode.value = ""
                        _detectedFileName.value = "Recording..."
                        Timber.d("Recording started")
                    }
                    is VideoRecordEvent.Finalize -> {
                        _isRecording.value = false
                        if (!event.hasError()) {
                            val uri = event.outputResults.outputUri
                            Timber.d("Recording finalized: $uri")
                            analyzeVideo(uri)
                        } else {
                            Timber.e("Recording error: ${event.error}")
                            _detectedFileName.value = "Error during recording"
                        }
                    }
                }
            }
    }

    fun stopRecording() {
        activeRecording?.stop()
        activeRecording = null
    }

    private fun analyzeVideo(uri: Uri) {
        viewModelScope.launch {
            _isParsing.value = true
            _progress.value = 0f
            _detectedFileName.value = "Parsing video..."
            
            try {
                videoExtractor.extractFrames(uri).collect { event ->
                    when (event) {
                        is ExtractionEvent.Progress -> {
                            _progress.value = event.progress
                        }
                        is ExtractionEvent.Frame -> {
                            val image = InputImage.fromBitmap(event.bitmap, 0)
                            try {
                                val visionText = recognizer.process(image).await()
                                if (visionText.text.isNotBlank()) {
                                    val stitched = codeStitcher.stitch(_currentScannedCode.value, visionText.text)
                                    _currentScannedCode.value = stitched
                                    
                                    val detectedName = fileSplitter.detectClassName(stitched)
                                    if (detectedName != null) {
                                        _detectedFileName.value = fileSplitter.generateFileName(detectedName)
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.e(e, "Error during OCR on frame")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error extracting frames from video")
                _detectedFileName.value = "Extraction failed"
            } finally {
                _progress.value = 1f
                _isParsing.value = false
                if (_detectedFileName.value == "Parsing video...") {
                    _detectedFileName.value = "Analysis complete"
                }
            }
        }
    }

    fun fixCodeWithAi() {
        magicFixAndSave()
    }

    fun magicFixAndSave() {
        val rawCode = _currentScannedCode.value
        if (rawCode.isBlank()) {
            Timber.w("Magic Fix triggered but scanned code is blank")
            return
        }

        viewModelScope.launch {
            _aiProcessState.value = Resource.Loading
            Timber.i("Starting Cloud AI Magic Fix for code snippet")

            try {
                val fixedCode = repository.fixCode(rawCode)
                val metadata = repository.generateMetadata(fixedCode)

                val newSnippet = SnippetEntity(
                    title = metadata.title,
                    category = metadata.category,
                    description = metadata.description,
                    codeContent = fixedCode
                )
                repository.insertSnippet(newSnippet)
                Timber.i("Snippet '${metadata.title}' saved successfully via Cloud AI")

                _currentScannedCode.value = fixedCode
                _aiProcessState.value = Resource.Success(fixedCode)
                _saveResult.value = Resource.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Magic Fix failed")
                _aiProcessState.value = Resource.Error(e.message ?: "Unknown AI Error")
            }
        }
    }

    fun saveSnippet(code: String, title: String) {
        if (code.isBlank()) return
        
        viewModelScope.launch {
            _saveResult.value = Resource.Loading
            try {
                val newSnippet = SnippetEntity(
                    title = if (title.startsWith("Analysis") || title.isBlank() || title.startsWith("Ready")) "Manual Save" else title,
                    category = "Manual",
                    description = "Manually saved snippet",
                    codeContent = code
                )
                repository.insertSnippet(newSnippet)
                Timber.i("Snippet '$title' saved manually")
                _saveResult.value = Resource.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Manual save failed")
                _saveResult.value = Resource.Error(e.message ?: "Save Error")
            }
        }
    }

    fun resetSaveResult() {
        _saveResult.value = Resource.Idle
    }

    fun clearSession() {
        _currentScannedCode.value = ""
        _detectedFileName.value = "Ready to scan"
        _aiProcessState.value = Resource.Idle
        _saveResult.value = Resource.Idle
        _isRecording.value = false
        _isParsing.value = false
        _progress.value = 0f
        Timber.d("Scanner session cleared")
    }
}
