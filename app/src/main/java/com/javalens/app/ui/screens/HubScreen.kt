package com.javalens.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.javalens.app.ui.theme.*
import com.javalens.app.ui.components.HubButton

@Composable
fun HubScreen(
    hasApiKey: Boolean,
    onScanClick: () -> Unit,
    onVaultClick: () -> Unit,
    onChatClick: () -> Unit,
    onVideoClick: () -> Unit,
    onGitHubClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(CyberBlack)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.width(48.dp)) // Offset to center title
                Text("JAVALENS", style = MaterialTheme.typography.displayMedium, color = Color.White)
                IconButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.background(CyberSlate, RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = NeonIndigo)
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            val statusColor = if (hasApiKey) NeonEmerald else Color.Red
            val statusText = if (hasApiKey) "Cloud AI: ACTIVE" else "Cloud AI: MISSING KEY"
            
            Surface(
                color = statusColor.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.5f))
            ) {
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelLarge,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
            HubButton("LIVE SCAN", "CAMERA OCR", Icons.Default.CameraAlt, NeonIndigo, onScanClick)
            Spacer(modifier = Modifier.height(16.dp))
            HubButton("SNIPPET VAULT", "DATABASE", Icons.Default.Storage, NeonEmerald, onVaultClick)
            Spacer(modifier = Modifier.height(16.dp))
            HubButton("VIDEO IMPORT", "NPU SCAN", Icons.Default.VideoFile, Color(0xFFFFB800), onVideoClick)
            Spacer(modifier = Modifier.height(16.dp))
            HubButton("AI ANALYZER", "PROJECT CHAT", Icons.Default.Code, if (hasApiKey) Color.White else Color.Gray, onChatClick)
            Spacer(modifier = Modifier.height(16.dp))
            HubButton("GITHUB SYNC", "PUSH CODE", Icons.Default.CloudSync, Color.Gray, onGitHubClick)
        }
    }
}
