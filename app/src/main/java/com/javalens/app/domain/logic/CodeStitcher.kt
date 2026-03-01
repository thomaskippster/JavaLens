package com.javalens.app.domain.logic

class CodeStitcher {
    /**
     * Stitches two frames by finding the maximum line overlap.
     * Uses a suffix-prefix matching algorithm.
     */
    fun stitch(existingCode: String, newFrame: String): String {
        val existingLines = existingCode.lines().filter { it.isNotBlank() }
        val newLines = newFrame.lines().filter { it.isNotBlank() }

        if (existingLines.isEmpty()) return newFrame
        if (newLines.isEmpty()) return existingCode

        // Check for overlap up to the last 15 lines for performance
        val maxOverlap = minOf(existingLines.size, newLines.size, 15)

        for (overlapSize in maxOverlap downTo 1) {
            val suffix = existingLines.takeLast(overlapSize)
            val prefix = newLines.take(overlapSize)

            if (suffix == prefix) {
                // Overlap found: drop the overlapping lines from the new frame
                val uniqueNewContent = newLines.drop(overlapSize)
                return (existingLines + uniqueNewContent).joinToString("
")
            }
        }

        // Fallback: No clean overlap found, append with a newline
        return existingCode + "
" + newFrame
    }
}
