package com.example.javalens.service

import com.example.javalens.data.Snippet
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiService {
    // Initialisierung des lokalen Modells via Google AI Edge SDK
    private val generativeModel = GenerativeModel(
        modelName = "gemini-nano", // Zugriff auf AICore
        apiKey = "" // Nicht benötigt bei On-Device AICore Nutzung
    )

    suspend fun analyzeAndFixCode(rawCode: String): String? {
        val prompt = """
            Du bist ein Java Expert auf einem Pixel 9. 
            1. Korrigiere OCR-Fehler (Semikolons, falsche Zeichen).
            2. Erkläre kurz die Logik.
            Code: $rawCode
        """.trimIndent()

        return withContext(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(prompt)
                response.text
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun createSnippetInfo(code: String): Snippet? {
        val prompt = "Erstelle Titel und Kategorie für diesen Java-Code. Format: Titel | Kategorie | Beschreibung"
        return try {
            val response = generativeModel.generateContent(prompt).text ?: return null
            val parts = response.split("|")
            Snippet(
                title = parts.getOrNull(0)?.trim() ?: "Unbekannt",
                category = parts.getOrNull(1)?.trim() ?: "General",
                description = parts.getOrNull(2)?.trim() ?: "Keine Beschreibung",
                code = code
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun analyzeCodeLocal(code: String): String {
        val prompt = """
            Du bist ein On-Device Java Expert. Analysiere diesen Code lokal auf dem Pixel 9:
            $code
            
            Gib eine kurze Zusammenfassung:
            1. Architektur-Muster
            2. Potenzielle Bugs
            3. Security-Check (lokal)
        """.trimIndent()

        return withContext(Dispatchers.IO) {
            try {
                val response = generativeModel.generateContent(prompt)
                response.text ?: "Keine lokale Analyse möglich."
            } catch (e: Exception) {
                "Fehler bei der lokalen Analyse: ${e.message}"
            }
        }
    }
}
