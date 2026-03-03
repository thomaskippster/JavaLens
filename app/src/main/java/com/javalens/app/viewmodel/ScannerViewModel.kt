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

    val downloadStatus: StateFlow<AiDownloadStatus> = repository.aiDownloadStatus

    private val _isAiAvailable = MutableStateFlow<Boolean?>(null)
    val isAiAvailable: StateFlow<Boolean?> = _isAiAvailable.asStateFlow()

    init {
        checkAiStatus()
    }

    fun checkAiStatus() {
        viewModelScope.launch {
            _isAiAvailable.value = repository.isAiAvailable()
        }
    }

    fun toggleScan() {
        _isScanning.value = !_isScanning.value
    }

    fun onNewFrameReceived(newText: String) {
        if (!_isScanning.value || _aiProcessState.value is Resource.Loading) return

        viewModelScope.launch {
            val stitchedCode = codeStitcher.stitch(_currentScannedCode.value, newText)
            _currentScannedCode.value = stitchedCode

            val detectedName = fileSplitter.detectClassName(stitchedCode)
            if (detectedName != null) {
                _detectedFileName.value = fileSplitter.generateFileName(detectedName)
            }
        }
    }

    fun fixCodeWithAi() {
        magicFixAndSave()
    }

    fun magicFixAndSave() {
        val rawCode = _currentScannedCode.value
        if (rawCode.isBlank()) return

        viewModelScope.launch {
            _aiProcessState.value = Resource.Loading
            _isScanning.value = false

            try {
                // 1. Gemini Nano: Syntax Repair
                val fixedCode = repository.fixCode(rawCode)

                // 2. Gemini Nano: Metadata Generation
                val metadata = repository.generateMetadata(fixedCode)

                // 3. Room: Persistent Storage
                val newSnippet = SnippetEntity(
                    title = metadata.title,
                    category = metadata.category,
                    description = metadata.description,
                    codeContent = fixedCode
                )
                repository.insertSnippet(newSnippet)

                _currentScannedCode.value = fixedCode
                _aiProcessState.value = Resource.Success(fixedCode)
            } catch (e: Exception) {
                _aiProcessState.value = Resource.Error(e.message ?: "Unknown AI Error")
            }
        }
    }

    fun clearSession() {
        _currentScannedCode.value = ""
        _detectedFileName.value = "Ready to scan"
        _isScanning.value = false
        _aiProcessState.value = Resource.Idle
    }
}
