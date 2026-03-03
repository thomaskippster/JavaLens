package com.javalens.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javalens.app.data.SnippetEntity
import com.javalens.app.domain.ai.AiDownloadStatus
import com.javalens.app.domain.logic.CodeStitcher
import com.javalens.app.domain.logic.FileSplitter
import com.javalens.app.domain.model.Resource
import com.javalens.app.domain.repository.SnippetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ScannerViewModel(
    private val repository: SnippetRepository
) : ViewModel() {

    private val codeStitcher = CodeStitcher()
    private val fileSplitter = FileSplitter()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _currentScannedCode = MutableStateFlow("")
    val currentScannedCode: StateFlow<String> = _currentScannedCode.asStateFlow()
    
    private val _detectedFileName = MutableStateFlow("Scanning...")
    val detectedFileName: StateFlow<String> = _detectedFileName.asStateFlow()

    private val _aiProcessState = MutableStateFlow<Resource<String>>(Resource.Idle)
    val aiProcessState: StateFlow<Resource<String>> = _aiProcessState.asStateFlow()

    private val _saveResult = MutableStateFlow<Resource<Unit>>(Resource.Idle)
    val saveResult: StateFlow<Resource<Unit>> = _saveResult.asStateFlow()

    val downloadStatus: StateFlow<AiDownloadStatus> = repository.aiDownloadStatus

    private val _isAiAvailable = MutableStateFlow<Boolean?>(null)
    val isAiAvailable: StateFlow<Boolean?> = _isAiAvailable.asStateFlow()

    init {
        checkAiStatus()
    }

    fun checkAiStatus() {
        viewModelScope.launch {
            try {
                _isAiAvailable.value = repository.isAiAvailable()
                Timber.d("AI Availability status: ${_isAiAvailable.value}")
            } catch (e: Exception) {
                Timber.e(e, "Error checking AI status")
            }
        }
    }

    fun toggleScan() {
        _isScanning.value = !_isScanning.value
        Timber.d("Scanning toggled: ${_isScanning.value}")
    }

    fun onNewFrameReceived(newText: String) {
        if (!_isScanning.value || _aiProcessState.value is Resource.Loading) return

        viewModelScope.launch {
            val stitchedCode = codeStitcher.stitch(_currentScannedCode.value, newText)
            _currentScannedCode.value = stitchedCode

            val detectedName = fileSplitter.detectClassName(stitchedCode)
            if (detectedName != null) {
                _detectedFileName.value = fileSplitter.generateFileName(detectedName)
                Timber.d("New class detected: $detectedName")
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
            _isScanning.value = false
            Timber.i("Starting AI Magic Fix for code snippet")

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
                Timber.i("Snippet '${metadata.title}' saved successfully via AI Fix")

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
                    title = if (title.startsWith("Scanning") || title.isBlank()) "Manual Save" else title,
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
        _isScanning.value = false
        _aiProcessState.value = Resource.Idle
        _saveResult.value = Resource.Idle
        Timber.d("Scanner session cleared")
    }
}
