package com.example.javalens.service

import kotlin.math.min

class FrameStitcher {
    private var lastCodeBlock: String = ""

    fun addFrame(newText: String): String {
        if (newText.isBlank()) return lastCodeBlock
        
        val similarity = calculateSimilarity(lastCodeBlock, newText)
        
        return if (similarity > 0.8) {
            lastCodeBlock // Kein signifikantes neues Material
        } else {
            val stitched = mergeText(lastCodeBlock, newText)
            lastCodeBlock = stitched
            stitched
        }
    }

    private fun mergeText(old: String, new: String): String {
        if (old.isEmpty()) return new
        
        val oldLines = old.lines().filter { it.isNotBlank() }
        val newLines = new.lines().filter { it.isNotBlank() }

        // Suche nach Overlap (Suffix-Prefix Matching)
        val maxOverlap = min(oldLines.size, newLines.size)
        for (overlapSize in maxOverlap downTo 1) {
            val suffix = oldLines.takeLast(overlapSize)
            val prefix = newLines.takeFirst(overlapSize)
            if (suffix == prefix) {
                val uniqueNewLines = newLines.drop(overlapSize)
                return (oldLines + uniqueNewLines).joinToString("
")
            }
        }

        // Wenn kein Overlap gefunden wurde, hängen wir es an (könnte ein neuer Block sein)
        return old + "
" + new
    }

    private fun calculateSimilarity(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        if (s1.isEmpty() || s2.isEmpty()) return 0.0

        val maxLength = maxOf(s1.length, s2.length)
        val distance = levenshtein(s1, s2)
        return (maxLength - distance).toDouble() / maxLength
    }

    private fun levenshtein(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }

        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    fun clear() {
        lastCodeBlock = ""
    }
}
