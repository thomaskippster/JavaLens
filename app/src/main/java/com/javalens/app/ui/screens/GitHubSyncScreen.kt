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
    var repo by remember { mutableStateOf("") }
    var commitMsg by remember { mutableStateOf("Sync via JavaLens Pixel 9") }
    
    var isSyncing by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("GITHUB SYNC", color = Color.White, style = MaterialTheme.typography.headlineLarge)
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

        if (isSyncing) {
            CircularProgressIndicator(color = NeonEmerald)
            Text(statusMessage, color = NeonEmerald, modifier = Modifier.padding(top = 16.dp))
        } else {
            Button(
                onClick = {
                    scope.launch {
                        isSyncing = true
                        exporter.saveToken(token)
                        
                        var success = true
                        snippets.forEach { snippet ->
                            statusMessage = "Pushing ${snippet.title}..."
                            val result = exporter.uploadFile(
                                owner = owner,
                                repo = repo,
                                path = "src/${snippet.title}",
                                code = snippet.codeContent,
                                commitMessage = commitMsg
                            )
                            if (!result) success = false
                        }
                        
                        statusMessage = if (success) "Sync Complete!" else "Some files failed."
                        isSyncing = false
                    }
                },
                enabled = token.isNotBlank() && owner.isNotBlank() && repo.isNotBlank() && snippets.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("PUSH SNIPPETS", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onBack) { 
            Text("BACK", color = Color.Gray) 
        }
    }
}
