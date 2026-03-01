package com.javalens.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.javalens.app.ui.theme.CyberBlack
import com.javalens.app.viewmodel.VideoImportViewModel

@Composable
fun VideoImportScreen(viewModel: VideoImportViewModel, onBack: () -> Unit) {
    val isParsing by viewModel.isParsing.collectAsState()
    
    // Video-Picker Launcher
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.parseVideo(it) }
    }
    
    Column(
        modifier = Modifier.fillMaxSize().background(CyberBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("VIDEO IMPORT", color = Color.White, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        
        if (isParsing) {
            CircularProgressIndicator(color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Analyzing Frames...", color = Color.Gray)
        } else {
            Button(
                onClick = { videoPickerLauncher.launch("video/mp4") }
            ) { 
                Text("Select MP4") 
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onBack) { 
            Text("BACK", color = Color.Gray) 
        }
    }
}
