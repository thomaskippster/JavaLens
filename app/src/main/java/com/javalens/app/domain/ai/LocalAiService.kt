package com.javalens.app.domain.ai

import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAiService {
    // Pixel 9 Exclusive: Access the local AICore (no API key required)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-nano",
        apiKey = "LOCAL_ONLY" 
    )

    /**
     * Fixes OCR artifacts (syntax errors) without changing logic.
     */
    suspend fun magicOcrFix(rawCode: String): String = withContext(Dispatchers.IO) {
        val prompt = """
            Du bist ein Java Expert auf einem Pixel 9. 
            Repariere ausschließlich Tipp- und Lesefehler (z. B. l statt 1, : statt ;). 
            Verändere niemals Variablennamen oder die Geschäftslogik. 
            Gib NUR den korrigierten Java-Code zurück.
            
            Code:
            $rawCode
        """.trimIndent()
        
        generativeModel.generateContent(prompt).text ?: rawCode
    }

    /**
     * Generates a title, category, and description for a code snippet.
     */
    suspend fun generateSnippetMetadata(code: String): SnippetMetadata = withContext(Dispatchers.IO) {
        val prompt = """
            Analysiere diesen Java-Code und generiere Metadaten. 
            Format: Titel | Kategorie | Beschreibung. 
            Kategorie muss ein einzelnes Wort sein (z.B. UI, Network, Security, Logic).
            
            Code: $code
        """.trimIndent()

        val response = generativeModel.generateContent(prompt).text ?: "Untitled | General | No description"
        val parts = response.split("|")
        
        SnippetMetadata(
            title = parts.getOrNull(0)?.trim() ?: "Untitled",
            category = parts.getOrNull(1)?.trim() ?: "General",
            description = parts.getOrNull(2)?.trim() ?: "No description provided."
        )
    }

    /**
     * Interactively ask questions about the current codebase context.
     */
    suspend fun askProjectChat(context: String, question: String): String = withContext(Dispatchers.IO) {
        val prompt = """
            Du hast Zugriff auf folgenden Java-Code (Kontext):
            $context
            
            User Frage: $question
        """.trimIndent()
        
        generativeModel.generateContent(prompt).text ?: "Die lokale KI konnte keine Antwort generieren."
    }
}

data class SnippetMetadata(val title: String, val category: String, val description: String)
