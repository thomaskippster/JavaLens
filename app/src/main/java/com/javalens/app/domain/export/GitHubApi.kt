package com.javalens.app.domain.export

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

interface GitHubApi {
    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getFile(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String,
        @Header("Authorization") token: String
    ): Response<JsonObject>

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun createOrUpdateFile(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String,
        @Header("Authorization") token: String,
        @Body request: GitHubFileRequest
    ): Response<ResponseBody>
}

data class GitHubFileRequest(
    val message: String,
    val content: String, // Base64 encoded code
    val sha: String? = null
)
