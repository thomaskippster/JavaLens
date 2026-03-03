package com.javalens.app.domain.repository

import com.javalens.app.data.SnippetDao
import com.javalens.app.data.SnippetEntity
import com.javalens.app.domain.ai.LocalAiService
import com.javalens.app.domain.ai.SnippetMetadata
import kotlinx.coroutines.flow.Flow

class SnippetRepository(
    private val snippetDao: SnippetDao,
    private val aiService: LocalAiService
) {
    fun getAllSnippets(): Flow<List<SnippetEntity>> = snippetDao.getAllSnippets()
    
    fun getSnippetById(id: Long): Flow<SnippetEntity?> = snippetDao.getSnippetById(id)

    suspend fun insertSnippet(snippet: SnippetEntity) {
        snippetDao.insertSnippet(snippet)
    }

    suspend fun fixCode(rawCode: String): String {
        return aiService.magicOcrFix(rawCode)
    }

    suspend fun generateMetadata(code: String): SnippetMetadata {
        return aiService.generateSnippetMetadata(code)
    }

    suspend fun isAiAvailable(): Boolean {
        return aiService.isAvailable()
    }
    
    val aiDownloadStatus = aiService.downloadStatus
}
