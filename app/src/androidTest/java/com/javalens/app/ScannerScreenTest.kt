package com.javalens.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.assertExists
import com.javalens.app.ui.screens.ScannerScreen
import com.javalens.app.viewmodel.ScannerViewModel
import com.javalens.app.domain.model.Resource
import com.javalens.app.domain.ai.AiDownloadStatus
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
        every { viewModel.isRecording } returns MutableStateFlow(false)
        every { viewModel.isParsing } returns MutableStateFlow(false)
        every { viewModel.progress } returns MutableStateFlow(0f)
        every { viewModel.currentScannedCode } returns MutableStateFlow("")
        every { viewModel.detectedFileName } returns MutableStateFlow("Ready to scan")
        every { viewModel.aiProcessState } returns MutableStateFlow(Resource.Idle)
        every { viewModel.saveResult } returns MutableStateFlow(Resource.Idle)
        every { viewModel.downloadStatus } returns MutableStateFlow(AiDownloadStatus.IDLE)
        every { viewModel.isAiAvailable } returns MutableStateFlow(true)

        composeTestRule.setContent {
            ScannerScreen(viewModel = viewModel)
        }

        // Check if main buttons exist
        composeTestRule.onNodeWithContentDescription("Clear").assertExists()
        composeTestRule.onNodeWithContentDescription("Start Scan").assertExists()
    }
}
