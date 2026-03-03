package com.javalens.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.javalens.app.ui.screens.ScannerScreen
import com.javalens.app.viewmodel.ScannerViewModel
import com.javalens.app.domain.repository.SnippetRepository
import com.javalens.app.domain.model.Resource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class ScannerScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testScannerScreenInitialState() {
        val viewModel = mockk<ScannerViewModel>(relaxed = true)
        
        // Mocking States
        every { viewModel.isScanning } returns MutableStateFlow(false)
        every { viewModel.currentScannedCode } returns MutableStateFlow("")
        every { viewModel.detectedFileName } returns MutableStateFlow("Ready to scan")
        every { viewModel.aiProcessState } returns MutableStateFlow(Resource.Idle)
        every { viewModel.downloadStatus } returns MutableStateFlow(com.javalens.app.domain.ai.AiDownloadStatus.IDLE)
        every { viewModel.isAiAvailable } returns MutableStateFlow(true)

        composeTestRule.setContent {
            ScannerScreen(viewModel = viewModel)
        }

        // Check if main buttons exist (we can search by content description or specific text)
        // CyberBodyButton usually has "SCAN" or similar, but we check by component presence
        composeTestRule.onNodeWithContentDescription("Clear").assertExists()
        composeTestRule.onNodeWithContentDescription("Magic Fix").assertExists()
    }
}
