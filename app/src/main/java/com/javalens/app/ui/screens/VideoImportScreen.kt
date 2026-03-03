package com.javalens.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javalens.app.ui.theme.CyberBlack
import com.javalens.app.ui.theme.CyberSlate
import com.javalens.app.ui.theme.NeonEmerald
import com.javalens.app.ui.theme.NeonIndigo
import com.javalens.app.viewmodel.VideoImportViewModel

@Composable
fun VideoImportScreen(viewModel: VideoImportViewModel, onBack: () -> Unit) {
    val isParsing by viewModel.isParsing.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val extractedCode by viewModel.extractedCode.collectAsState()
    
    // Video-Picker Launcher for all video types
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.parseVideo(it) }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            "VIDEO IMPORT", 
            color = Color.White, 
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "EXTRACT CODE FROM SCREENCAST", 
            color = NeonIndigo, 
            style = MaterialTheme.typography.labelMedium
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        if (isParsing) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CyberSlate,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = NeonIndigo)
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("ANALYZING VIDEO...", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = NeonEmerald,
                        trackColor = Color.DarkGray,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${(progress * 100).toInt()}%", color = NeonEmerald, fontSize = 12.sp)
                }
            }
        } else {
            if (extractedCode.isEmpty()) {
                Button(
                    onClick = { videoPickerLauncher.launch("video/*") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberSlate)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.VideoFile, null, modifier = Modifier.size(48.dp), tint = NeonIndigo)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("SELECT VIDEO FROM GALLERY", color = Color.White)
                    }
                }
            } else {
                // Show Preview of Extracted Code
                Surface(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    color = CyberSlate,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonEmerald.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("EXTRACTED CODE", color = NeonEmerald, style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = extractedCode,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = { videoPickerLauncher.launch("video/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo)
                ) {
                    Text("SELECT ANOTHER VIDEO")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onBack) { 
            Text("BACK TO HUB", color = Color.Gray) 
        }
    }
}
