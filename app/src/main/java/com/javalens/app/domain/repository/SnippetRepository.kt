package com.javalens.app.domain.repository

import com.javalens.app.data.SnippetDao
import com.javalens.app.data.SnippetEntity
import com.javalens.app.domain.ai.CloudAiService
import com.javalens.app.domain.ai.SnippetMetadata
import com.javalens.app.domain.utils.SettingsManager
import kotlinx.coroutines.flow.Flow

class SnippetRepository(
    private val snippetDao: SnippetDao,
    private val aiService: CloudAiService,
    private val settingsManager: SettingsManager
) {
    fun getAllSnippets(): Flow<List<SnippetEntity>> = snippetDao.getAllSnippets()
    
    fun getSnippetById(id: Long): Flow<SnippetEntity?> = snippetDao.getSnippetById(id)

    suspend fun insertSnippet(snippet: SnippetEntity) {
        snippetDao.insertSnippet(snippet)
    }
    
    suspend fun deleteSnippetById(id: Long) {
        snippetDao.deleteSnippetById(id)
    }

    suspend fun fixCode(rawCode: String): String {
        return aiService.magicOcrFix(rawCode)
    }

    suspend fun generateMetadata(code: String): SnippetMetadata {
        return aiService.generateSnippetMetadata(code)
    }
    
    suspend fun askAiQuestion(context: String, question: String): String {
        return aiService.askProjectChat(context, question)
    }

    fun hasAiApiKey(): Boolean {
        return settingsManager.hasGeminiKey()
    }

    fun saveAiApiKey(key: String) {
        settingsManager.saveGeminiKey(key)
    }

    fun getAiApiKey(): String? {
        return settingsManager.getGeminiKey()
    }
}
