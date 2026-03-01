package com.javalens.modules

import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import com.javalens.app.domain.ai.LocalAiService
import com.javalens.app.domain.logic.CodeStitcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class JavaLensModule : Module() {
  private val localAiService = LocalAiService()
  private val codeStitcher = CodeStitcher()
  private val scope = CoroutineScope(Dispatchers.Main)

  override fun definition() = ModuleDefinition {
    Name("JavaLens")

    // --- 1. LOCAL AI (MAGIC OCR FIX) ---
    AsyncFunction("magicOcrFix") { rawCode: String ->
      localAiService.magicOcrFix(rawCode)
    }

    // --- 2. LOCAL AI (METADATA GENERATION) ---
    AsyncFunction("generateMetadata") { code: String ->
      val metadata = localAiService.generateSnippetMetadata(code)
      mapOf(
        "title" to metadata.title,
        "category" to metadata.category,
        "description" to metadata.description
      )
    }

    // --- 3. PROJECT CHAT (OFFLINE) ---
    AsyncFunction("askProjectChat") { context: String, question: String ->
      localAiService.askProjectChat(context, question)
    }

    // --- 4. CODE STITCHING (NATIVE PERFORMANCE) ---
    Function("stitchCode") { existing: String, newFrame: String ->
      codeStitcher.stitch(existing, newFrame)
    }

    // --- 5. EVENTS (OCR REAL-TIME) ---
    Events("onTextExtracted")

    // Note: LiveTextAnalyzer integration usually requires CameraView hooking, 
    // which is more complex in a module, but we provide the core logic here.
  }
}
