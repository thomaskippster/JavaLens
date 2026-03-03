package com.javalens.app.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.javalens.app.domain.ai.LocalAiService
import com.javalens.app.domain.export.GitHubApi
import com.javalens.app.domain.export.GitHubExporter
import com.javalens.app.domain.repository.SnippetRepository
import com.javalens.app.ui.screens.*
import com.javalens.app.viewmodel.ScannerViewModel
import com.javalens.app.viewmodel.VideoImportViewModel
import com.javalens.app.viewmodel.ProjectChatViewModel
import kotlinx.collections.immutable.ImmutableList
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

sealed class Screen(val route: String) {
    object Hub : Screen("hub")
    object Scanner : Screen("scanner")
    object Vault : Screen("vault")
    object Chat : Screen("chat")
    object VideoImport : Screen("video")
    object GitHub : Screen("github")
    object SnippetDetail : Screen("snippet/{snippetId}") {
        fun createRoute(id: Long) = "snippet/$id"
    }
}

@Composable
fun AppNavigation(
    scannerViewModel: ScannerViewModel,
    videoViewModel: VideoImportViewModel,
    vaultSnippets: ImmutableList<com.javalens.app.data.SnippetEntity>
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val repository: SnippetRepository = koinInject()
    
    // We can use a state to pass the code context from Detail to Chat
    var activeChatContext by remember { mutableStateOf("") }
    
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
            val aiStatus by scannerViewModel.downloadStatus.collectAsState()
            HubScreen(
                aiStatus = aiStatus,
                onScanClick = { navController.navigate(Screen.Scanner.route) },
                onVaultClick = { navController.navigate(Screen.Vault.route) },
                onChatClick = { 
                    activeChatContext = "" // Reset context when coming from Hub
                    navController.navigate(Screen.Chat.route) 
                },
                onVideoClick = { navController.navigate(Screen.VideoImport.route) },
                onGitHubClick = { navController.navigate(Screen.GitHub.route) }
            )
        }
        composable(Screen.Scanner.route) { ScannerScreen(viewModel = scannerViewModel) }
        composable(Screen.Vault.route) { 
            SnippetLibraryScreen(
                snippets = vaultSnippets,
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
                    activeChatContext = code
                    navController.navigate(Screen.Chat.route)
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Chat.route) { 
            val liveScannerCode by scannerViewModel.currentScannedCode.collectAsState()
            // If activeChatContext is set (from Detail), use it. Otherwise use Live Scanner code.
            val finalContext = if (activeChatContext.isNotEmpty()) activeChatContext else liveScannerCode
            
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
            GitHubSyncScreen(
                exporter = githubExporter, 
                snippets = vaultSnippets,
                onBack = { navController.popBackStack() }
            ) 
        }
    }
}
