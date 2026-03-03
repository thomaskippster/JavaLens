package com.javalens.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import com.javalens.app.domain.repository.SnippetRepository
import com.javalens.app.ui.navigation.AppNavigation
import com.javalens.app.ui.theme.JavaLensTheme
import com.javalens.app.viewmodel.ScannerViewModel
import com.javalens.app.viewmodel.VideoImportViewModel
import kotlinx.collections.immutable.toImmutableList
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    private val repository: SnippetRepository by inject()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkRequiredPermissions()

        setContent {
            JavaLensTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    val scannerViewModel: ScannerViewModel = koinViewModel()
                    val videoViewModel: VideoImportViewModel = koinViewModel()
                    
                    val snippets by repository.getAllSnippets().collectAsState(initial = emptyList())
                    
                    AppNavigation(
                        scannerViewModel = scannerViewModel,
                        videoViewModel = videoViewModel,
                        vaultSnippets = snippets.toImmutableList()
                    )
                }
            }
        }
    }

    private fun checkRequiredPermissions() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        val toRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (toRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(toRequest.toTypedArray())
        }
    }
}
