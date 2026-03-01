package com.javalens.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javalens.app.domain.export.GitHubExporter
import com.javalens.app.ui.theme.CyberBlack
import com.javalens.app.ui.theme.CyberSlate
import com.javalens.app.ui.theme.NeonEmerald
import com.javalens.app.ui.theme.NeonIndigo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GitHubSyncScreen(
    exporter: GitHubExporter,
    snippets: List<com.javalens.app.data.SnippetEntity>,
    onBack: () -> Unit
) {
    var token by remember { mutableStateOf(exporter.getToken() ?: "") }
    var owner by remember { mutableStateOf("") }
    var repoName by remember { mutableStateOf("") }
    var commitMsg by remember { mutableStateOf("Sync via JavaLens Pixel 9") }
    var isSyncing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "GITHUB SYNC",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
            color = Color.White,
            modifier = Modifier.padding(bottom = 32.dp, top = 20.dp)
        )

        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Personal Access Token", color = Color.Gray) },
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonIndigo,
                unfocusedBorderColor = CyberSlate,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = owner,
                onValueChange = { owner = it },
                label = { Text("Owner", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonIndigo,
                    unfocusedBorderColor = CyberSlate,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = repoName,
                onValueChange = { repoName = it },
                label = { Text("Repo", color = Color.Gray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonIndigo,
                    unfocusedBorderColor = CyberSlate,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = commitMsg,
            onValueChange = { commitMsg = it },
            label = { Text("Commit Message", color = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonIndigo,
                unfocusedBorderColor = CyberSlate,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isSyncing) {
            CircularProgressIndicator(color = NeonEmerald, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(statusMessage, color = NeonEmerald, fontSize = 12.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp))
        } else {
            Button(
                onClick = {
                    scope.launch {
                        isSyncing = true
                        exporter.saveToken(token)
                        
                        var successCount = 0
                        snippets.forEach { snippet ->
                            statusMessage = "Pushing ${snippet.title}..."
                            val success = exporter.uploadFile(
                                owner = owner,
                                repo = repoName,
                                path = "src/${snippet.title}",
                                code = snippet.code,
                                commitMessage = commitMsg
                            )
                            if (success) successCount++
                        }
                        
                        statusMessage = "Synced $successCount/${snippets.size} files."
                        isSyncing = false
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = token.isNotBlank() && owner.isNotBlank() && repoName.isNotBlank() && snippets.isNotEmpty()
            ) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = Color.Black)
                Spacer(modifier = Modifier.width(12.dp))
                Text("PUSH ${snippets.size} SNIPPETS", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        if (statusMessage.isNotEmpty() && !isSyncing) {
            Text(statusMessage, color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp).align(Alignment.CenterHorizontally))
        }

        Spacer(modifier = Modifier.weight(1f))
        
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("BACK TO HUB", color = Color.Gray)
        }
    }
}
