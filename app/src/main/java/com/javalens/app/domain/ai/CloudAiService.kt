package com.javalens.app.domain.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.javalens.app.domain.utils.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class CloudAiService(private val settingsManager: SettingsManager) {

    private fun getModel(): GenerativeModel? {
        val apiKey = settingsManager.getApiKey() ?: return null
        return GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = apiKey
        )
    }

    private val AI_UNAVAILABLE_MSG = "Please set your Gemini API Key in the settings first."

    suspend fun magicOcrFix(rawCode: String): String = withContext(Dispatchers.IO) {
        val model = getModel() ?: return@withContext AI_UNAVAILABLE_MSG
        if (rawCode.isBlank()) return@withContext ""

        val prompt = """
            You are a Java expert. Fix OCR errors (typos, wrong brackets, misread characters) in the following code.
            Return ONLY the cleaned Java code, without explanations or markdown formatting.
            Code:
            $rawCode
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            response.text?.trim() ?: rawCode
        } catch (e: Exception) {
            Timber.e(e, "Cloud AI magicOcrFix failed")
            "AI Error: ${e.localizedMessage ?: "Unknown error"}"
        }
    }

    suspend fun generateSnippetMetadata(code: String): SnippetMetadata = withContext(Dispatchers.IO) {
        val model = getModel() ?: return@withContext SnippetMetadata("API Key Missing", "System", AI_UNAVAILABLE_MSG)

        val prompt = """
            Analyze this Java code. 
            Provide a title, a single-word category, and a short description.
            Format the response exactly as: Title | Category | Description
            Code:
            $code
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            val result = response.text ?: "Untitled | General | No description"
            val parts = result.split("|")
            SnippetMetadata(
                title = parts.getOrNull(0)?.trim() ?: "Untitled",
                category = parts.getOrNull(1)?.trim() ?: "General",
                description = parts.getOrNull(2)?.trim() ?: "No description"
            )
        } catch (e: Exception) {
            Timber.e(e, "Cloud AI generateSnippetMetadata failed")
            SnippetMetadata("Error", "System", "Cloud AI failed: ${e.localizedMessage}")
        }
    }

    suspend fun askProjectChat(context: String, question: String): String = withContext(Dispatchers.IO) {
        val model = getModel() ?: return@withContext AI_UNAVAILABLE_MSG

        val prompt = """
            Context (Java Code):
            $context
            
            Question:
            $question
            
            Answer as a senior software engineer. Be concise but helpful.
        """.trimIndent()

        try {
            val response = model.generateContent(prompt)
            response.text ?: "I couldn't generate a response."
        } catch (e: Exception) {
            Timber.e(e, "Cloud AI askProjectChat failed")
            "Cloud AI Error: ${e.localizedMessage}"
        }
    }
}
