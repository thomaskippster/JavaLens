package com.example.javalens.service

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface GitHubService {
    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun uploadFile(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Body request: GitHubContentRequest
    ): Response<Unit>
}

data class GitHubContentRequest(
    val message: String,
    val content: String, // Base64 encoded
    val sha: String? = null
)
