package com.javalens.app.ui.navigation

import android.net.Uri
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.javalens.app.domain.export.GitHubExporter
import com.javalens.app.domain.repository.SnippetRepository
import com.javalens.app.ui.screens.*
import com.javalens.app.viewmodel.*
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

sealed class Screen(val route: String) {
    object Hub : Screen("hub")
    object Scanner : Screen("scanner")
    object Vault : Screen("vault")
    object VideoImport : Screen("video")
    object GitHub : Screen("github")
    
    object Chat : Screen("chat?codeContext={codeContext}") {
        fun createRoute(codeContext: String? = null): String {
            return if (codeContext != null) {
                "chat?codeContext=${Uri.encode(codeContext)}"
            } else {
                "chat"
            }
        }
    }
    
    object SnippetDetail : Screen("snippet/{snippetId}") {
        fun createRoute(id: Long) = "snippet/$id"
    }
}

@Composable
fun AppNavigation(
    scannerViewModel: ScannerViewModel,
    videoViewModel: VideoImportViewModel
) {
    val navController = rememberNavController()
    val repository: SnippetRepository = koinInject()
    val githubExporter: GitHubExporter = koinInject()

    NavHost(navController = navController, startDestination = Screen.Hub.route) {
        composable(Screen.Hub.route) {
            val hubViewModel: HubViewModel = koinViewModel()
            HubScreen(
                viewModel = hubViewModel,
                onScanClick = { navController.navigate(Screen.Scanner.route) },
                onVaultClick = { navController.navigate(Screen.Vault.route) },
                onChatClick = { 
                    if (hubViewModel.hasApiKey()) {
                        navController.navigate(Screen.Chat.createRoute())
                    } else {
                        // HubScreen shows status, but we could also show a snackbar here if needed
                    }
                },
                onVideoClick = { navController.navigate(Screen.VideoImport.route) },
                onGitHubClick = { navController.navigate(Screen.GitHub.route) }
            )
        }
        
        composable(Screen.Scanner.route) { 
            ScannerScreen(viewModel = scannerViewModel) 
        }
        
        composable(Screen.Vault.route) { 
            val vaultViewModel: VaultViewModel = koinViewModel()
            val snippets by vaultViewModel.snippets.collectAsStateWithLifecycle()
            
            SnippetLibraryScreen(
                snippets = snippets,
                onSnippetClick = { id -> 
                    navController.navigate(Screen.SnippetDetail.createRoute(id)) 
                },
                onBack = { navController.popBackStack() }
            ) 
        }
        
        composable(
            route = Screen.SnippetDetail.route,
            arguments = listOf(navArgument("snippetId") { type = NavType.LongType }),
            deepLinks = listOf(navDeepLink { uriPattern = "javalens://snippet/{snippetId}" })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("snippetId") ?: -1L
            SnippetDetailScreen(
                snippetId = id,
                repository = repository,
                onChatWithSnippet = { code ->
                    navController.navigate(Screen.Chat.createRoute(code))
                },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("codeContext") { 
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val codeFromArgs = backStackEntry.arguments?.getString("codeContext")
            val liveScannerCode by scannerViewModel.currentScannedCode.collectAsState()
            
            // Priority: 1. Args (from Detail), 2. Live Scanner
            val finalContext = codeFromArgs ?: liveScannerCode
            
            val chatViewModel: ProjectChatViewModel = koinViewModel()
            ProjectChatScreen(
                codeContext = finalContext,
                viewModel = chatViewModel
            ) 
        }
        
        composable(Screen.VideoImport.route) { 
            VideoImportScreen(viewModel = videoViewModel, onBack = { navController.popBackStack() }) 
        }
        
        composable(Screen.GitHub.route) { 
            val vaultViewModel: VaultViewModel = koinViewModel()
            val snippets by vaultViewModel.snippets.collectAsStateWithLifecycle()
            
            GitHubSyncScreen(
                exporter = githubExporter, 
                snippets = snippets,
                onBack = { navController.popBackStack() }
            ) 
        }
    }
}
