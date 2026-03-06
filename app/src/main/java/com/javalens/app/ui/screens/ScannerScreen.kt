package com.javalens.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import com.javalens.app.domain.model.Resource
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
    val isRecording by viewModel.isRecording.collectAsState()
    val isParsing by viewModel.isParsing.collectAsState()
    val parsingProgress by viewModel.progress.collectAsState()
    val scannedCode by viewModel.currentScannedCode.collectAsState()
    val fileName by viewModel.detectedFileName.collectAsState()
    val aiProcessState by viewModel.aiProcessState.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()
    
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle AI and Save results with Snackbar
    LaunchedEffect(aiProcessState, saveResult) {
        if (aiProcessState is Resource.Error) {
            snackbarHostState.showSnackbar((aiProcessState as Resource.Error).message)
        }
        if (saveResult is Resource.Success) {
            snackbarHostState.showSnackbar("Successfully saved!")
            viewModel.resetSaveResult()
        }
        if (saveResult is Resource.Error) {
            snackbarHostState.showSnackbar((saveResult as Resource.Error).message)
            viewModel.resetSaveResult()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(padding)
        ) {
            
            // 1. Live Camera Feed
            Box(modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isRecording) Modifier.border(4.dp, Color.Red) else Modifier
                )
            ) {
                CameraPreviewView(
                    onVideoCaptureReady = { capture ->
                        videoCapture = capture
                    }
                )
                
                if (isRecording) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color.Red, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("REC", color = Color.White, fontSize = 14.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            }
            
            // 2. AR Overlay: Code Preview (Visible when code is present or parsing)
            AnimatedVisibility(
                visible = scannedCode.isNotEmpty() || isParsing,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut()
            ) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(32.dp)
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
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
                                text = if (isParsing) "ANALYZING VIDEO..." else "FILE: $fileName",
                                color = if (isParsing) NeonIndigo else Color.White,
                                fontSize = 10.sp
                            )
                            if (scannedCode.isNotEmpty() && !isParsing) {
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
                        }
                        
                        if (isParsing) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { parsingProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = NeonIndigo,
                                trackColor = CyberBlack
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(parsingProgress * 100).toInt()}%",
                                color = NeonIndigo,
                                fontSize = 10.sp,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }

                        if (scannedCode.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = scannedCode,
                                color = NeonEmerald,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            )
                            
                            if (!isRecording && !isParsing) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { viewModel.fixCodeWithAi() },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonIndigo),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = aiProcessState !is Resource.Loading
                                    ) {
                                        Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("MAGIC FIX")
                                    }
                                    
                                    Button(
                                        onClick = { viewModel.saveSnippet(scannedCode, fileName) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonEmerald),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        enabled = saveResult !is Resource.Loading
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null, tint = Color.Black)
                                        Spacer(Modifier.width(8.dp))
                                        Text("SAVE", color = Color.Black)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. AI Processing Feedback
            if (aiProcessState is Resource.Loading) {
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
                            text = "CLOUD AI REPAIRING CODE...",
                            color = NeonIndigo,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }

            // 4. Action Bar
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
                    IconButton(
                        onClick = { viewModel.clearSession() },
                        modifier = Modifier.background(CyberSlate, CircleShape),
                        enabled = !isRecording && !isParsing
                    ) {
                        Icon(Icons.Default.Delete, "Clear", tint = Color.Gray)
                    }

                    CyberBodyButton(
                        isScanning = isRecording,
                        onClick = { 
                            if (!isRecording) {
                                videoCapture?.let { viewModel.startRecording(context, it) }
                            } else {
                                viewModel.stopRecording()
                            }
                        },
                        enabled = !isParsing
                    )

                    IconButton(
                        onClick = { viewModel.saveSnippet(scannedCode, fileName) },
                        modifier = Modifier.background(CyberSlate, CircleShape),
                        enabled = scannedCode.isNotEmpty() && saveResult !is Resource.Loading && !isRecording && !isParsing
                    ) {
                        Icon(Icons.Default.Save, "Save", tint = NeonEmerald)
                    }
                }
            }
        }
    }
}
