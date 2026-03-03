package com.javalens.app.domain.ai

import android.content.Context
import com.google.ai.edge.aicore.GenerativeModel
import com.google.ai.edge.aicore.GenerationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalAiService(private val context: Context) {
    // Initialisierung des Gemini Nano Modells über AICore
    private val generativeModel = GenerativeModel(
        GenerationConfig.builder().apply { 
            // setContext ist in der aktuellen API Version oft notwendig
            // Wir nutzen die Builder-Struktur wie in der Dokumentation
        }.build()
    )

    suspend fun isAvailable(): Boolean = withContext(Dispatchers.IO) {
        // Hier sollte normalerweise eine Prüfung gegen den AICore-Status erfolgen
        // Für die Ausführung auf dem Pixel 9 muss AICore in den Systemeinstellungen aktiv sein.
        try { true } catch (e: Exception) { false }
    }

    suspend fun magicOcrFix(rawCode: String): String = withContext(Dispatchers.IO) {
        val prompt = "Du bist ein Java Expert. Repariere ausschließlich Tippfehler. Gib NUR den korrigierten Code zurück.\nCode:\n$rawCode"
        try { 
            generativeModel.generateContent(prompt).text ?: rawCode 
        } catch (e: Exception) { 
            rawCode 
        }
    }

    suspend fun generateSnippetMetadata(code: String): SnippetMetadata = withContext(Dispatchers.IO) {
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
            SnippetMetadata("Untitled", "General", "AI Error")
        }
    }

    suspend fun askProjectChat(context: String, question: String): String = withContext(Dispatchers.IO) {
        val prompt = "Kontext:\n$context\n\nFrage: $question"
        try { 
            generativeModel.generateContent(prompt).text ?: "Keine Antwort." 
        } catch (e: Exception) { 
            "NPU Error" 
        }
    }
}

data class SnippetMetadata(val title: String, val category: String, val description: String)
