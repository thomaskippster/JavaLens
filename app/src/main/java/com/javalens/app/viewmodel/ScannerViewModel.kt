package com.javalens.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javalens.app.data.SnippetDao
import com.javalens.app.data.SnippetEntity
import com.javalens.app.domain.ai.LocalAiService
import com.javalens.app.domain.logic.CodeStitcher
import com.javalens.app.domain.logic.FileSplitter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScannerViewModel(
    private val snippetDao: SnippetDao,
    private val aiService: LocalAiService = LocalAiService(),
    private val codeStitcher: CodeStitcher = CodeStitcher(),
    private val fileSplitter: FileSplitter = FileSplitter()
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _currentScannedCode = MutableStateFlow("")
    val currentScannedCode: StateFlow<String> = _currentScannedCode.asStateFlow()
    
    private val _detectedFileName = MutableStateFlow("Scanning...")
    val detectedFileName: StateFlow<String> = _detectedFileName.asStateFlow()

    private val _isProcessingAi = MutableStateFlow(false)
    val isProcessingAi: StateFlow<Boolean> = _isProcessingAi.asStateFlow()

    private val _isAiAvailable = MutableStateFlow<Boolean?>(null)
    val isAiAvailable: StateFlow<Boolean?> = _isAiAvailable.asStateFlow()

    init {
        checkAiStatus()
    }

    fun checkAiStatus() {
        viewModelScope.launch {
            _isAiAvailable.value = aiService.isAvailable()
        }
    }

    fun toggleScan() {
        _isScanning.value = !_isScanning.value
    }

    fun onNewFrameReceived(newText: String) {
        if (!_isScanning.value || _isProcessingAi.value) return

        viewModelScope.launch {
            val stitchedCode = codeStitcher.stitch(_currentScannedCode.value, newText)
            _currentScannedCode.value = stitchedCode

            val detectedName = fileSplitter.detectClassName(stitchedCode)
            if (detectedName != null) {
                _detectedFileName.value = fileSplitter.generateFileName(detectedName)
            }
        }
    }

    /**
     * Triggers Gemini Nano to fix OCR errors and save the snippet with metadata.
     */
    fun magicFixAndSave() {
        val rawCode = _currentScannedCode.value
        if (rawCode.isBlank()) return

        viewModelScope.launch {
            _isProcessingAi.value = true
            _isScanning.value = false // Stop scanning during AI processing

            // 1. Gemini Nano: Syntax Repair
            val fixedCode = aiService.magicOcrFix(rawCode)

            // 2. Gemini Nano: Metadata Generation (Title, Category, Desc)
            val metadata = aiService.generateSnippetMetadata(fixedCode)

            // 3. Room: Persistent Storage
            val newSnippet = SnippetEntity(
                title = metadata.title,
                category = metadata.category,
                description = metadata.description,
                codeContent = fixedCode
            )
            snippetDao.insertSnippet(newSnippet)

            _currentScannedCode.value = fixedCode
            _isProcessingAi.value = false
        }
    }

    fun clearSession() {
        _currentScannedCode.value = ""
        _detectedFileName.value = "Ready to scan"
        _isScanning.value = false
        _isProcessingAi.value = false
    }
}
