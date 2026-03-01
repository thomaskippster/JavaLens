package com.example.javalens.ui

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.javalens.data.Snippet
import com.example.javalens.data.SnippetDao
import com.example.javalens.service.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class JavaLensViewModel(
    private val snippetDao: SnippetDao,
    private val frameStitcher: FrameStitcher = FrameStitcher(),
    private val geminiService: GeminiService = GeminiService(),
    private val fileSplitter: FileSplitter = FileSplitter()
) : ViewModel() {

    private val githubService = Retrofit.Builder()
        .baseUrl("https://api.github.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GitHubService::class.java)

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _currentCode = MutableStateFlow("")
    val currentCode: StateFlow<String> = _currentCode.asStateFlow()

    private val _snippets = snippetDao.getAllSnippets()
    val snippets = _snippets

    fun toggleScanning() {
        _isScanning.value = !_isScanning.value
        if (_isScanning.value) {
            _currentCode.value = ""
            frameStitcher.clear()
        }
    }

    fun onTextRecognized(newText: String) {
        if (_isScanning.value) {
            _currentCode.value = frameStitcher.addFrame(newText)
        }
    }

    fun analyzeAndSave() {
        viewModelScope.launch {
            val rawCode = _currentCode.value
            val correctedCode = geminiService.analyzeAndFixCode(rawCode) ?: rawCode
            
            // Perfrom local AI Magic (Architecture/Bugs/Security)
            val deepAnalysis = geminiService.analyzeCodeLocal(correctedCode)
            
            val snippet = geminiService.createSnippetInfo(correctedCode) ?: Snippet(
                title = fileSplitter.generateFileName(fileSplitter.detectClassName(correctedCode)),
                code = correctedCode,
                category = "Logic",
                description = deepAnalysis.take(200) // Using AI Analysis for description
            )
            
            snippetDao.insertSnippet(snippet)
            _currentCode.value = ""
            _isScanning.value = false
            frameStitcher.clear()
        }
    }

    fun exportToGitHub(snippet: Snippet, token: String, owner: String, repo: String) {
        viewModelScope.launch {
            val contentBase64 = Base64.encodeToString(snippet.code.toByteArray(), Base64.NO_WRAP)
            try {
                githubService.uploadFile(
                    token = "token $token",
                    owner = owner,
                    repo = repo,
                    path = "src/${snippet.title}",
                    request = GitHubContentRequest(
                        message = "Upload ${snippet.title} via JavaLens",
                        content = contentBase64
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
