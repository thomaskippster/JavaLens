package com.javalens.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javalens.app.data.SnippetEntity
import com.javalens.app.domain.utils.ClipboardUtils
import com.javalens.app.ui.theme.CyberBlack
import com.javalens.app.ui.theme.CyberSlate
import com.javalens.app.ui.theme.NeonEmerald
import com.javalens.app.ui.theme.NeonIndigo

@Composable
fun SnippetCard(snippet: SnippetEntity) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CyberSlate),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Category Badge (On-Device AI Generated)
                Surface(
                    color = NeonIndigo.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = snippet.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = NeonIndigo
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = snippet.title,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = snippet.description,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            // Mini-Code Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberBlack, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = snippet.codeContent.take(150) + "...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = NeonEmerald
                )
            }
            
            IconButton(
                onClick = { ClipboardUtils.copyToClipboard(context, snippet.codeContent) },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Kopieren",
                    tint = NeonIndigo
                )
            }
        }
    }
}
