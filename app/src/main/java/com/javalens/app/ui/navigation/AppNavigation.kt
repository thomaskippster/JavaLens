package com.javalens.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.javalens.app.ui.screens.*
import com.javalens.app.viewmodel.ScannerViewModel

sealed class Screen(val route: String) {
    object Hub : Screen("hub")
    object Scanner : Screen("scanner")
    object Vault : Screen("vault")
    object Chat : Screen("chat")
}

@Composable
fun AppNavigation(
    scannerViewModel: ScannerViewModel,
    vaultSnippets: List<com.javalens.app.data.SnippetEntity>
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Hub.route) {
        composable(Screen.Hub.route) {
            HubScreen(
                onScanClick = { navController.navigate(Screen.Scanner.route) },
                onVaultClick = { navController.navigate(Screen.Vault.route) },
                onChatClick = { navController.navigate(Screen.Chat.route) }
            )
        }

        composable(Screen.Scanner.route) {
            ScannerScreen(
                viewModel = scannerViewModel
            )
        }
        
        composable(Screen.Vault.route) {
            SnippetLibraryScreen(
                snippets = vaultSnippets,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Chat.route) {
            val codeContext by scannerViewModel.currentScannedCode.collectAsState()
            ProjectChatScreen(
                codeContext = codeContext
            )
        }
    }
}
