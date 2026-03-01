package com.javalens.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javalens.app.ui.theme.CyberBlack
import com.javalens.app.ui.theme.CyberSlate
import com.javalens.app.ui.theme.NeonIndigo
import com.javalens.app.viewmodel.VideoImportViewModel

@Composable
fun VideoImportScreen(
    viewModel: VideoImportViewModel,
    onBack: () -> Unit
) {
    val isParsing by viewModel.isParsing.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val extractedCode by viewModel.extractedCode.collectAsState()

    // Real File Picker for MP4
    val videoPicker = rememberLauncherForActivityResult(
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
        Text(
            text = "VIDEO IMPORT",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
            color = Color.White,
            modifier = Modifier.padding(bottom = 40.dp, top = 20.dp)
        )

        Icon(
            imageVector = Icons.Default.VideoLibrary,
            contentDescription = null,
            tint = NeonIndigo.copy(alpha = 0.5f),
            modifier = Modifier.size(100.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "SELECT MP4 FOR NPU FRAME SCAN",
            color = Color.Gray,
            fontSize = 12.sp,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        if (isParsing) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "AICORE PROCESSING...",
                    color = NeonIndigo,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = NeonIndigo,
                    trackColor = CyberSlate,
                )
            }
        } else {
            Button(
                onClick = { videoPicker.launch("video/mp4") },
                colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("SELECT VIDEO FILE", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        if (extractedCode.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(CyberSlate, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = extractedCode.take(500) + "...",
                    color = Color.Green,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
        
        TextButton(onClick = onBack) {
            Text("BACK TO HUB", color = Color.Gray)
        }
    }
}
