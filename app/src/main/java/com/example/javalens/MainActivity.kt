package com.example.javalens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.javalens.data.AppDatabase
import com.example.javalens.ui.*
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

enum class Screen { HUB, SCANNER, VAULT }

class MainActivity : ComponentActivity() {
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var db: AppDatabase

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "javalens-db"
        ).fallbackToDestructiveMigration().build()
        
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        val viewModel = JavaLensViewModel(db.snippetDao())

        setContent {
            MaterialTheme(colorScheme = darkColorScheme(
                background = DeepBlack,
                primary = NeonIndigo,
                surface = DarkSurface
            )) {
                JavaLensApp(viewModel, cameraExecutor)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@Composable
fun JavaLensApp(viewModel: JavaLensViewModel, cameraExecutor: ExecutorService) {
    var currentScreen by remember { mutableStateOf(Screen.HUB) }
    val isScanning by viewModel.isScanning.collectAsState()
    val snippets by viewModel.snippets.collectAsState(initial = emptyList())
    val currentCode by viewModel.currentCode.collectAsState()
    
    val recognizer = remember { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    Box(modifier = Modifier.fillMaxSize().background(DeepBlack)) {
        when (currentScreen) {
            Screen.HUB -> HubScreen(
                onScanClick = { currentScreen = Screen.SCANNER },
                onVaultClick = { currentScreen = Screen.VAULT }
            )
            Screen.SCANNER -> ScannerView(
                isScanning = isScanning,
                currentCode = currentCode,
                onToggleScan = { 
                    if (isScanning) viewModel.analyzeAndSave() else viewModel.toggleScanning() 
                },
                onBack = { 
                    if (isScanning) viewModel.toggleScanning()
                    currentScreen = Screen.HUB 
                },
                cameraExecutor = cameraExecutor,
                recognizer = recognizer,
                onTextRecognized = { viewModel.onTextRecognized(it) }
            )
            Screen.VAULT -> VaultView(
                snippets = snippets,
                onBack = { currentScreen = Screen.HUB }
            )
        }
    }
}

@Composable
fun HubScreen(onScanClick: () -> Unit, onVaultClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "JAVALENS",
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Black,
                fontStyle = FontStyle.Italic,
                letterSpacing = 4.sp
            ),
            color = Color.White
        )
        Text(
            "PIXEL 9 EXCLUSIVE EDITION",
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
            subtitle = "OPTIMIZE WITH GEMINI NANO",
            icon = Icons.Default.Code,
            color = Color.White.copy(alpha = 0.8f),
            onClick = { /* Could open specific AI tool */ }
        )
    }
}

@Composable
fun HubButton(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(title, fontWeight = FontWeight.Black, color = Color.White, fontSize = 18.sp)
                Text(subtitle, color = Color.Gray, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun ScannerView(
    isScanning: Boolean,
    currentCode: String,
    onToggleScan: () -> Unit,
    onBack: () -> Unit,
    cameraExecutor: ExecutorService,
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    onTextRecognized: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            CameraPreview(
                modifier = Modifier.fillMaxSize(),
                onTextRecognized = onTextRecognized,
                cameraExecutor = cameraExecutor,
                recognizer = recognizer
            )
            
            if (currentCode.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(0.9f).height(200.dp).align(Alignment.BottomCenter).padding(bottom = 16.dp),
                    color = DarkSurface.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        currentCode,
                        modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState()),
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = NeonIndigo
                    )
                }
            }
        }
        
        Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CyberBodyButton(isScanning = isScanning, onClick = onToggleScan)
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onBack) {
                Text("BACK TO HUB", color = Color.Gray)
            }
        }
    }
}

@Composable
fun VaultView(snippets: List<com.example.javalens.data.Snippet>, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            SnippetLibraryScreen(snippets)
        }
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            TextButton(onClick = onBack) {
                Text("BACK TO HUB", color = Color.Gray)
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onTextRecognized: (String) -> Unit,
    cameraExecutor: ExecutorService,
    recognizer: com.google.mlkit.vision.text.TextRecognizer
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    processImageProxy(recognizer, imageProxy, onTextRecognized)
                }
            }

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalyzer
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    AndroidView({ previewView }, modifier = modifier)
}

@androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
private fun processImageProxy(
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    imageProxy: ImageProxy,
    onTextRecognized: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { text ->
                onTextRecognized(text.text)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}
