package com.javalens.app.domain.export

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface GitHubApi {
    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun createOrUpdateFile(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Header("Authorization") token: String,
        @Body request: GitHubFileRequest
    ): Response<ResponseBody>
}

data class GitHubFileRequest(
    val message: String,
    val content: String // Base64 encoded code
)

class GitHubExporter(private val context: Context, private val api: GitHubApi) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val secureStorage = EncryptedSharedPreferences.create(
        "github_secrets",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveToken(token: String) = secureStorage.edit().putString("gh_token", token).apply()
    fun getToken(): String? = secureStorage.getString("gh_token", null)

    suspend fun uploadFile(
        owner: String,
        repo: String,
        path: String,
        code: String,
        commitMessage: String
    ): Boolean {
        val token = getToken() ?: return false
        
        // Base64 encoding WITHOUT NEWLINES (Required for GitHub API)
        val base64Content = android.util.Base64.encodeToString(
            code.toByteArray(), 
            android.util.Base64.NO_WRAP
        )
        
        val request = GitHubFileRequest(commitMessage, base64Content)
        
        return try {
            val response = api.createOrUpdateFile(owner, repo, path, "Bearer $token", request)
            response.isSuccessful
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
