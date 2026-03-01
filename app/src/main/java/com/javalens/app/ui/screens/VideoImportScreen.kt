package com.javalens.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.javalens.app.ui.theme.CyberBlack
import com.javalens.app.viewmodel.VideoImportViewModel

@Composable
fun VideoImportScreen(viewModel: VideoImportViewModel, onBack: () -> Unit) {
    val isParsing by viewModel.isParsing.collectAsState()
    
    Column(
        modifier = Modifier.fillMaxSize().background(CyberBlack),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("VIDEO IMPORT", color = Color.White, style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))
        if (isParsing) {
            CircularProgressIndicator(color = Color.White)
        } else {
            Button(onClick = { /* Picker logic here */ }) { Text("Select MP4") }
        }
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onBack) { Text("BACK", color = Color.Gray) }
    }
}