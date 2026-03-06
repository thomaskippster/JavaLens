package com.javalens.app.domain.export

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.javalens.app.data.SnippetDao
import timber.log.Timber

class GitHubSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val exporter: GitHubExporter,
    private val snippetDao: SnippetDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val owner = inputData.getString("owner") ?: return Result.failure()
        val repo = inputData.getString("repo") ?: return Result.failure()
        val commitMsg = inputData.getString("commitMsg") ?: "Sync via JavaLens"

        Timber.i("GitHubSyncWorker started for $owner/$repo")

        return try {
            val snippets = snippetDao.getAllSnippetsSync()
            var success = true

            snippets.forEach { snippet ->
                // Sanitize title completely and append .java
                val sanitizedTitle = snippet.title
                    .replace(Regex("[^a-zA-Z0-9_-]"), "_")
                    .ifBlank { "snippet_${snippet.id}" }
                
                val path = "src/${sanitizedTitle}.java"
                
                val result = exporter.uploadFile(
                    owner = owner,
                    repo = repo,
                    path = path,
                    code = snippet.codeContent,
                    commitMessage = commitMsg
                )
                if (!result) {
                    success = false
                    Timber.w("Failed to upload ${snippet.title}")
                }
            }

            if (success) {
                Timber.i("GitHubSyncWorker completed successfully")
                Result.success()
            } else {
                Timber.e("GitHubSyncWorker failed for some files")
                Result.retry()
            }
        } catch (e: Exception) {
            Timber.e(e, "GitHubSyncWorker error")
            Result.failure()
        }
    }
}
