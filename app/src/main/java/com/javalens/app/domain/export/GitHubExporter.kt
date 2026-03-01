package com.javalens.app.domain.export

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

class GitHubExporter(private val api: GitHubApi) {

    suspend fun uploadFile(
        token: String,
        owner: String,
        repo: String,
        path: String,
        code: String,
        commitMessage: String
    ): Boolean {
        val base64Content = android.util.Base64.encodeToString(code.toByteArray(), android.util.Base64.NO_WRAP)
        val request = GitHubFileRequest(commitMessage, base64Content)
        
        return try {
            val response = api.createOrUpdateFile(owner, repo, path, "Bearer $token", request)
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
}
