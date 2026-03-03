package com.javalens.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.javalens.app.data.AppDatabase
import com.javalens.app.domain.video.VideoCodeExtractor
import com.javalens.app.ui.navigation.AppNavigation
import com.javalens.app.ui.theme.JavaLensTheme
import com.javalens.app.viewmodel.ScannerViewModel
import com.javalens.app.viewmodel.VideoImportViewModel

class MainActivity : ComponentActivity() {

    private val db by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "javalens-db"
        ).build()
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkCameraPermission()

        setContent {
            JavaLensTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    val scannerViewModel: ScannerViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return ScannerViewModel(application, db.snippetDao()) as T
                            }
                        }
                    )
                    
                    val videoViewModel = remember { VideoImportViewModel(VideoCodeExtractor(applicationContext)) }
                    
                    val snippets by db.snippetDao().getAllSnippets().collectAsState(initial = emptyList())
                    
                    AppNavigation(
                        scannerViewModel = scannerViewModel,
                        videoViewModel = videoViewModel,
                        vaultSnippets = snippets
                    )
                }
            }
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}
