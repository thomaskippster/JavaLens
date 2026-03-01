package com.javalens.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.javalens.app.ui.theme.CyberBlack

@Composable
fun GitHubSyncScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(CyberBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("GITHUB SYNC", color = Color.White, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = { }) { Text("Push Commits") }
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onBack) { Text("BACK", color = Color.Gray) }
    }
}