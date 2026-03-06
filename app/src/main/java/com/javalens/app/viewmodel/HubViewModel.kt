package com.javalens.app.viewmodel

import androidx.lifecycle.ViewModel
import com.javalens.app.domain.repository.SnippetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class HubViewModel(private val repository: SnippetRepository) : ViewModel() {

    private val _apiKey = MutableStateFlow(repository.getAiApiKey() ?: "")
    val apiKey = _apiKey.asStateFlow()

    fun updateApiKey(newKey: String) {
        repository.saveAiApiKey(newKey)
        _apiKey.value = newKey
    }

    fun hasApiKey(): Boolean {
        return repository.hasAiApiKey()
    }
}
