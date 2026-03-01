package com.javalens.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Storage
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
    onChatClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
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
        
        Spacer(modifier = Modifier.height(64.dp))

        HubButton(
            title = "LIVE SCAN",
            subtitle = "EXTRACT CODE VIA CAMERA",
            icon = Icons.Default.CameraAlt,
            color = NeonIndigo,
            onClick = onScanClick
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        HubButton(
            title = "SNIPPET VAULT",
            subtitle = "MANAGE YOUR JAVA LIBRARY",
            icon = Icons.Default.Storage,
            color = NeonEmerald,
            onClick = onVaultClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        HubButton(
            title = "AI ANALYZER",
            subtitle = "CHAT WITH YOUR CODEBASE",
            icon = Icons.Default.Code,
            color = Color.White.copy(alpha = 0.8f),
            onClick = onChatClick
        )
        
        Spacer(modifier = Modifier.height(64.dp))
        
        Text(
            text = "TENSOR G4 NPU ACTIVE",
            style = MaterialTheme.typography.labelSmall,
            color = NeonIndigo.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun HubButton(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSlate),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontSize = 18.sp
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}
