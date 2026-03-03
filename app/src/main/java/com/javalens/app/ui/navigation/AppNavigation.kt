package com.javalens.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.javalens.app.domain.ai.LocalAiService
import com.javalens.app.domain.export.GitHubApi
import com.javalens.app.domain.export.GitHubExporter
import com.javalens.app.ui.screens.*
import com.javalens.app.viewmodel.ScannerViewModel
import com.javalens.app.viewmodel.VideoImportViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed class Screen(val route: String) {
    object Hub : Screen("hub")
    object Scanner : Screen("scanner")
    object Vault : Screen("vault")
    object Chat : Screen("chat")
    object VideoImport : Screen("video")
    object GitHub : Screen("github")
}

@Composable
fun AppNavigation(
    scannerViewModel: ScannerViewModel,
    videoViewModel: VideoImportViewModel,
    vaultSnippets: List<com.javalens.app.data.SnippetEntity>
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    
    // GitHub Logic Setup
    val githubApi = remember {
        Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GitHubApi::class.java)
    }
    val githubExporter = remember { GitHubExporter(context, githubApi) }

    NavHost(navController = navController, startDestination = Screen.Hub.route) {
        composable(Screen.Hub.route) {
            val isAiAvailable by scannerViewModel.isAiAvailable.collectAsState()
            HubScreen(
                isAiAvailable = isAiAvailable,
                onScanClick = { navController.navigate(Screen.Scanner.route) },
                onVaultClick = { navController.navigate(Screen.Vault.route) },
                onChatClick = { navController.navigate(Screen.Chat.route) },
                onVideoClick = { navController.navigate(Screen.VideoImport.route) },
                onGitHubClick = { navController.navigate(Screen.GitHub.route) }
            )
        }
        composable(Screen.Scanner.route) { ScannerScreen(viewModel = scannerViewModel) }
        composable(Screen.Vault.route) { SnippetLibraryScreen(snippets = vaultSnippets, onBack = { navController.popBackStack() }) }
        composable(Screen.Chat.route) { 
            val codeContext by scannerViewModel.currentScannedCode.collectAsState()
            ProjectChatScreen(
                codeContext = codeContext,
                aiService = remember { LocalAiService(context) }
            ) 
        }
        composable(Screen.VideoImport.route) { 
            VideoImportScreen(viewModel = videoViewModel, onBack = { navController.popBackStack() }) 
        }
        composable(Screen.GitHub.route) { 
            GitHubSyncScreen(
                exporter = githubExporter, 
                snippets = vaultSnippets,
                onBack = { navController.popBackStack() }
            ) 
        }
    }
}
