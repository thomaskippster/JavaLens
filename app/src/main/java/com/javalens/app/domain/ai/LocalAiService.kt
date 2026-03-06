package com.javalens.app.domain.ai

import android.content.Context
import com.google.ai.edge.aicore.DownloadCallback
import com.google.ai.edge.aicore.DownloadConfig
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.GenerationConfig
import com.google.ai.edge.aicore.GenerativeAIException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber

enum class AiDownloadStatus {
    IDLE, STARTING, DOWNLOADING, COMPLETED, FAILED, PENDING
}

class LocalAiService(private val context: Context) {
    
    private val _downloadStatus = MutableStateFlow(AiDownloadStatus.IDLE)
    val downloadStatus = _downloadStatus.asStateFlow()

    private val _downloadProgress = MutableStateFlow(0f)
    val downloadProgress = _downloadProgress.asStateFlow()

    private val downloadCallback = object : DownloadCallback {
        override fun onDownloadStarted(bytesToDownload: Long) {
            _downloadStatus.value = AiDownloadStatus.STARTING
            Timber.d("AI Download started: $bytesToDownload bytes")
        }

        override fun onDownloadProgress(totalBytesDownloaded: Long) {
            _downloadStatus.value = AiDownloadStatus.DOWNLOADING
        }

        override fun onDownloadCompleted() {
            _downloadStatus.value = AiDownloadStatus.COMPLETED
            Timber.i("AI Download completed")
        }

        override fun onDownloadFailed(failureStatus: String, e: GenerativeAIException) {
            _downloadStatus.value = AiDownloadStatus.FAILED
            Timber.e(e, "AI Download failed: $failureStatus")
        }

        override fun onDownloadPending() {
            _downloadStatus.value = AiDownloadStatus.PENDING
            Timber.d("AI Download pending")
        }
    }

    // Attempting a standard constructor for exp02: GenerativeModel(generationConfig, downloadConfig)
    private val generativeModel by lazy {
        GenerativeModel(
            GenerationConfig.builder()
                .build(),
            DownloadConfig(downloadCallback)
        )
    }

    private val AI_UNAVAILABLE_MSG = "AI is currently downloading or unavailable. Please wait."

    suspend fun triggerModelDownload() = withContext(Dispatchers.IO) {
        if (_downloadStatus.value == AiDownloadStatus.IDLE || _downloadStatus.value == AiDownloadStatus.FAILED) {
            Timber.d("Triggering AI model initialization/download...")
            try {
                // Accessing the lazy property triggers the initialization and download config
                val model = generativeModel
                Timber.d("GenerativeModel initialized")
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize GenerativeModel")
                _downloadStatus.value = AiDownloadStatus.FAILED
            }
        }
    }

    suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        if (_downloadStatus.value == AiDownloadStatus.IDLE) {
            triggerModelDownload()
        }
        _downloadStatus.value == AiDownloadStatus.COMPLETED
    }

    suspend fun magicOcrFix(rawCode: String): String = withContext(Dispatchers.IO) {
        if (!isAvailable()) return@withContext AI_UNAVAILABLE_MSG
        if (rawCode.isBlank()) return@withContext ""
        
        val prompt = """
            Du bist ein Java-Experte. Repariere OCR-Fehler (Tippfehler, falsche Klammern) im folgenden Code.
            Gib NUR den bereinigten Java-Code zurück, ohne Erklärungen oder Markdown-Formatierung.
            Code:
            $rawCode
        """.trimIndent()
        
        try { 
            generativeModel.generateContent(prompt).text?.trim() ?: rawCode 
        } catch (e: Exception) { 
            Timber.e(e, "Error during magicOcrFix")
            rawCode 
        }
    }

    suspend fun generateSnippetMetadata(code: String): SnippetMetadata = withContext(Dispatchers.IO) {
        if (!isAvailable()) return@withContext SnippetMetadata("AI Loading...", "System", AI_UNAVAILABLE_MSG)
        
        val prompt = "Analysiere diesen Code. Format: Titel | Kategorie | Beschreibung. Kategorie ist ein Wort.\nCode: $code"
        try {
            val response = generativeModel.generateContent(prompt).text ?: "Untitled | General | No description"
            val parts = response.split("|")
            SnippetMetadata(
                title = parts.getOrNull(0)?.trim() ?: "Untitled",
                category = parts.getOrNull(1)?.trim() ?: "General",
                description = parts.getOrNull(2)?.trim() ?: "No description"
            )
        } catch (e: Exception) {
            Timber.e(e, "Error generating metadata")
            SnippetMetadata("Untitled", "General", "AI Error")
        }
    }

    suspend fun askProjectChat(context: String, question: String): String = withContext(Dispatchers.IO) {
        if (!isAvailable()) return@withContext AI_UNAVAILABLE_MSG
        
        val prompt = "Kontext:\n$context\n\nFrage: $question"
        try { 
            generativeModel.generateContent(prompt).text ?: "Keine Antwort." 
        } catch (e: Exception) { 
            Timber.e(e, "Error in project chat")
            "NPU Error" 
        }
    }
}
