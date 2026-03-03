package com.javalens.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.javalens.app.domain.utils.ClipboardUtils
import com.javalens.app.ui.components.CameraPreviewView
import com.javalens.app.ui.components.CyberBodyButton
import com.javalens.app.ui.theme.CyberBlack
import com.javalens.app.ui.theme.CyberSlate
import com.javalens.app.ui.theme.NeonEmerald
import com.javalens.app.ui.theme.NeonIndigo
import com.javalens.app.viewmodel.ScannerViewModel

@Composable
fun ScannerScreen(
    viewModel: ScannerViewModel
) {
    val context = LocalContext.current
    val isScanning by viewModel.isScanning.collectAsState()
    val scannedCode by viewModel.currentScannedCode.collectAsState()
    val fileName by viewModel.detectedFileName.collectAsState()
    val isProcessingAi by viewModel.isProcessingAi.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(CyberBlack)) {
        
        // 1. Live Camera Feed
        CameraPreviewView(
            isScanningActive = isScanning,
            onTextExtracted = { text -> 
                viewModel.onNewFrameReceived(text) 
            }
        )
        
        // 2. AR Overlay: Code Preview (Neon Emerald Glow)
        AnimatedVisibility(
            visible = scannedCode.isNotEmpty(),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(32.dp)
                    .fillMaxWidth()
                    .heightIn(max = 250.dp),
                color = CyberSlate.copy(alpha = 0.85f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonIndigo.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "FILE: $fileName",
                            color = Color.White,
                            fontSize = 10.sp
                        )
                        IconButton(
                            onClick = { ClipboardUtils.copyToClipboard(context, scannedCode) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = NeonIndigo,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = scannedCode,
                        color = NeonEmerald,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        }

        // 3. AI Processing Feedback (Tensor Engine Warning)
        if (isProcessingAi) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = NeonIndigo)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "GEMINI NANO REPAIRING CODE...",
                        color = NeonIndigo,
                        fontSize = 12.sp,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // 4. Action Bar (Bottom Hub)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Clear Button
                IconButton(
                    onClick = { viewModel.clearSession() },
                    modifier = Modifier.background(CyberSlate, CircleShape)
                ) {
                    Icon(Icons.Default.Delete, "Clear", tint = Color.Gray)
                }

                // Main Scan Button
                CyberBodyButton(
                    isScanning = isScanning,
                    onClick = { viewModel.toggleScan() }
                )

                // Magic Fix & Save Button
                IconButton(
                    onClick = { viewModel.magicFixAndSave() },
                    modifier = Modifier.background(CyberSlate, CircleShape)
                ) {
                    Icon(Icons.Default.AutoFixHigh, "Magic Fix", tint = NeonIndigo)
                }
            }
        }
    }
}
