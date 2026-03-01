package com.javalens.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CyberBlack = Color(0xFF000000)
val CyberSlate = Color(0xFF0D1117)
val NeonIndigo = Color(0xFF6366F1)
val NeonEmerald = Color(0xFF10B981)

private val DarkColorScheme = darkColorScheme(
    primary = NeonIndigo,
    background = CyberBlack,
    surface = CyberSlate,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.LightGray,
    tertiary = NeonEmerald
)

@Composable
fun JavaLensTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
