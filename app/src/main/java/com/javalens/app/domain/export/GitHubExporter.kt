package com.javalens.app.domain.export

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import timber.log.Timber

class GitHubExporter(private val context: Context, private val api: GitHubApi) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val secureStorage = EncryptedSharedPreferences.create(
        context,
        "github_secrets",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Speichert das GitHub Token verschlüsselt in den SharedPreferences.
     */
    fun saveToken(token: String) {
        secureStorage.edit().putString("gh_token", token).apply()
    }

    /**
     * Lädt das verschlüsselte GitHub Token.
     */
    fun getToken(): String? {
        return secureStorage.getString("gh_token", null)
    }

    suspend fun uploadFile(
        owner: String,
        repo: String,
        path: String,
        code: String,
        commitMessage: String
    ): Boolean {
        val token = getToken() ?: return false
        val authHeader = "Bearer $token"

        // Sanitize path (replace spaces and invalid special chars with underscore)
        val sanitizedPath = path.replace(Regex("[^a-zA-Z0-9./_-]"), "_")
        
        // Base64 encoding WITHOUT NEWLINES (Required for GitHub API)
        val base64Content = android.util.Base64.encodeToString(
            code.toByteArray(), 
            android.util.Base64.NO_WRAP
        )
        
        var existingSha: String? = null
        try {
            val fileResponse = api.getFile(owner, repo, sanitizedPath, authHeader)
            if (fileResponse.isSuccessful) {
                existingSha = fileResponse.body()?.get("sha")?.asString
                Timber.d("File exists, existing sha: $existingSha")
            }
        } catch (e: Exception) {
            Timber.d("File does not exist or error fetching sha: ${e.message}")
        }

        val request = GitHubFileRequest(
            message = commitMessage,
            content = base64Content,
            sha = existingSha
        )
        
        return try {
            val response = api.createOrUpdateFile(owner, repo, sanitizedPath, authHeader, request)
            if (!response.isSuccessful) {
                Timber.e("GitHub upload failed: ${response.code()} - ${response.errorBody()?.string()}")
            }
            response.isSuccessful
        } catch (e: Exception) {
            Timber.e(e, "GitHub upload error")
            false
        }
    }
}
