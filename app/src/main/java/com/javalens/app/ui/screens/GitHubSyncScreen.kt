package com.javalens.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.*
import com.javalens.app.domain.export.GitHubExporter
import com.javalens.app.domain.export.GitHubSyncWorker
import com.javalens.app.ui.theme.CyberBlack
import com.javalens.app.ui.theme.CyberSlate
import com.javalens.app.ui.theme.NeonEmerald
import com.javalens.app.ui.theme.NeonIndigo
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubSyncScreen(
    exporter: GitHubExporter,
    snippets: List<com.javalens.app.data.SnippetEntity>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var token by remember { mutableStateOf(exporter.getToken() ?: "") }
    var owner by remember { mutableStateOf("") }
    var repo by remember { mutableStateOf("") }
    var commitMsg by remember { mutableStateOf("Sync via JavaLens Pixel 9") }
    
    val workManager = remember { WorkManager.getInstance(context) }
    
    // Track work status
    val workInfos by workManager.getWorkInfosForUniqueWorkFlow("github_sync").collectAsState(initial = emptyList())
    val currentWorkInfo = workInfos.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("GITHUB SYNC", color = Color.White, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("BACKGROUND WORKER MODE", color = NeonIndigo, style = MaterialTheme.typography.labelSmall)
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Personal Access Token", color = Color.Gray) },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonIndigo,
                unfocusedBorderColor = CyberSlate,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = owner,
            onValueChange = { owner = it },
            label = { Text("Owner / Username", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonIndigo,
                unfocusedBorderColor = CyberSlate,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = repo,
            onValueChange = { repo = it },
            label = { Text("Repository Name", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonIndigo,
                unfocusedBorderColor = CyberSlate,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        val isRunning = currentWorkInfo?.state == WorkInfo.State.RUNNING
        val isEnqueued = currentWorkInfo?.state == WorkInfo.State.ENQUEUED
        
        if (isRunning || isEnqueued) {
            CircularProgressIndicator(color = NeonEmerald)
            Text("Syncing in background...", color = NeonEmerald, modifier = Modifier.padding(top = 16.dp))
        } else {
            Button(
                onClick = {
                    exporter.saveToken(token)
                    
                    val syncData = workDataOf(
                        "owner" to owner,
                        "repo" to repo,
                        "commitMsg" to commitMsg
                    )
                    
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                    
                    val syncRequest = OneTimeWorkRequestBuilder<GitHubSyncWorker>()
                        .setInputData(syncData)
                        .setConstraints(constraints)
                        .build()
                    
                    workManager.enqueueUniqueWork(
                        "github_sync",
                        ExistingWorkPolicy.REPLACE,
                        syncRequest
                    )
                    
                    Timber.d("GitHubSyncWorker enqueued")
                },
                enabled = token.isNotBlank() && owner.isNotBlank() && repo.isNotBlank() && snippets.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("START BACKGROUND SYNC", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        
        currentWorkInfo?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Last Status: ${it.state.name}",
                color = if (it.state == WorkInfo.State.SUCCEEDED) NeonEmerald else Color.Gray,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onBack) { 
            Text("BACK", color = Color.Gray) 
        }
    }
}
