package com.javalens.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.javalens.app.ui.theme.*
import com.javalens.app.ui.components.HubButton

@Composable
fun HubScreen(
    onScanClick: () -> Unit,
    onVaultClick: () -> Unit,
    onChatClick: () -> Unit,
    onVideoClick: () -> Unit,
    onGitHubClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().background(CyberBlack).padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("JAVALENS", style = MaterialTheme.typography.displayMedium, color = Color.White)
        Spacer(modifier = Modifier.height(48.dp))
        HubButton("LIVE SCAN", "CAMERA OCR", Icons.Default.CameraAlt, NeonIndigo, onScanClick)
        Spacer(modifier = Modifier.height(16.dp))
        HubButton("SNIPPET VAULT", "DATABASE", Icons.Default.Storage, NeonEmerald, onVaultClick)
        Spacer(modifier = Modifier.height(16.dp))
        HubButton("VIDEO IMPORT", "NPU SCAN", Icons.Default.VideoFile, Color(0xFFFFB800), onVideoClick)
        Spacer(modifier = Modifier.height(16.dp))
        HubButton("AI ANALYZER", "PROJECT CHAT", Icons.Default.Code, Color.White, onChatClick)
        Spacer(modifier = Modifier.height(16.dp))
        HubButton("GITHUB SYNC", "PUSH CODE", Icons.Default.CloudSync, Color.Gray, onGitHubClick)
    }
}