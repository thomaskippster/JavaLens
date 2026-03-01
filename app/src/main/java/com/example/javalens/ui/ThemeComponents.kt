package com.example.javalens.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.javalens.data.Snippet

// --- 1. THEME & COLORS (Cyber-Dark Look) ---
val NeonIndigo = Color(0xFF6366F1)
val NeonEmerald = Color(0xFF10B981)
val DeepBlack = Color(0xFF000000)
val DarkSurface = Color(0xFF0D1117)

@Composable
fun SnippetLibraryScreen(snippets: List<Snippet>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepBlack)
            .padding(16.dp)
    ) {
        item {
            Text(
                "SNIPPET VAULT",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        items(snippets) { snippet ->
            SnippetCard(snippet)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SnippetCard(snippet: Snippet) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Kategorie Badge
                Surface(
                    color = NeonIndigo.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        snippet.category.uppercase(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                        color = NeonIndigo
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    snippet.title,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                    color = Color.White
                )
            }
            
            Spacer(Modifier.height(12.dp))
            Text(snippet.description, color = Color.Gray, fontSize = 12.sp)
            
            Spacer(Modifier.height(16.dp))
            // Mini-Code-Vorschau
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBlack, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    snippet.code.take(100) + "...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = NeonEmerald
                )
            }
        }
    }
}

@Composable
fun CyberBodyButton(
    isScanning: Boolean,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
            .clickable(onClick = onClick)
    ) {
        // Pulsierender Hintergrund-Ring
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = CircleShape,
            color = if (isScanning) Color.Red.copy(alpha = 0.2f) else NeonIndigo.copy(alpha = 0.2f)
        ) {}
        
        // Der eigentliche Button
        Surface(
            modifier = Modifier.size(70.dp),
            shape = CircleShape,
            color = if (isScanning) Color.Red else NeonIndigo,
            shadowElevation = 8.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = if (isScanning) "STOP" else "SCAN",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
        }
    }
}
