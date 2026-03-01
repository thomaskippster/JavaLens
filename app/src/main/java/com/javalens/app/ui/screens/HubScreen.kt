package com.javalens.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javalens.app.ui.theme.CyberBlack
import com.javalens.app.ui.theme.CyberSlate
import com.javalens.app.ui.theme.NeonEmerald
import com.javalens.app.ui.theme.NeonIndigo

@Composable
fun HubScreen(
    onScanClick: () -> Unit,
    onVaultClick: () -> Unit,
    onChatClick: () -> Unit,
    onVideoClick: () -> Unit,
    onGitHubClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "JAVALENS",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 4.sp
            ),
            color = Color.White
        )
        Text(
            text = "PIXEL 9 EXCLUSIVE EDITION",
            style = MaterialTheme.typography.labelSmall,
            color = NeonIndigo.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(48.dp))

        // LIVE SCAN
        HubButton("LIVE SCAN", "EXTRACT CODE VIA CAMERA", Icons.Default.CameraAlt, NeonIndigo, onScanClick)
        Spacer(modifier = Modifier.height(16.dp))
        
        // VAULT
        HubButton("SNIPPET VAULT", "MANAGE YOUR JAVA LIBRARY", Icons.Default.Storage, NeonEmerald, onVaultClick)
        Spacer(modifier = Modifier.height(16.dp))
        
        // VIDEO IMPORT
        HubButton("VIDEO IMPORT", "OFFLINE NPU EXTRACTION", Icons.Default.VideoFile, Color(0xFFFFB800), onVideoClick)
        Spacer(modifier = Modifier.height(16.dp))

        // PROJECT CHAT
        HubButton("AI ANALYZER", "CHAT WITH YOUR CODEBASE", Icons.Default.Code, Color.White.copy(alpha = 0.8f), onChatClick)
        Spacer(modifier = Modifier.height(16.dp))

        // GITHUB SYNC
        HubButton("GITHUB SYNC", "PUSH TO REPOSITORIES", Icons.Default.CloudSync, Color.Gray, onGitHubClick)
        
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "TENSOR G4 NPU ACTIVE",
            style = MaterialTheme.typography.labelSmall,
            color = NeonIndigo.copy(alpha = 0.4f),
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
fun HubButton(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSlate),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).background(color.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp)) }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                Text(text = subtitle, color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}
