package com.javalens.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.javalens.app.domain.model.ChatMessage
import com.javalens.app.domain.repository.SnippetRepository
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class ProjectChatViewModel(
    private val repository: SnippetRepository
) : ViewModel() {

    private val _chatMessages = MutableStateFlow<PersistentList<ChatMessage>>(persistentListOf())
    val chatMessages: StateFlow<PersistentList<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping.asStateFlow()

    fun sendMessage(context: String, question: String) {
        if (question.isBlank()) return

        viewModelScope.launch {
            // 1. Add User Message
            val userMsg = ChatMessage(text = question, isUser = true)
            _chatMessages.value = _chatMessages.value.add(userMsg)
            
            _isTyping.value = true
            Timber.d("User sent chat question")

            try {
                // 2. Call AI
                val response = repository.askAiQuestion(context, question)
                
                val aiMsg = ChatMessage(text = response, isUser = false)
                _chatMessages.value = _chatMessages.value.add(aiMsg)
            } catch (e: Exception) {
                Timber.e(e, "Chat AI Error")
                _chatMessages.value = _chatMessages.value.add(
                    ChatMessage(text = "Error: ${e.message}", isUser = false)
                )
            } finally {
                _isTyping.value = false
            }
        }
    }
}
