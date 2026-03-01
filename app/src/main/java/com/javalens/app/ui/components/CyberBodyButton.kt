package com.javalens.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.javalens.app.ui.theme.NeonIndigo

@Composable
fun CyberBodyButton(
    isScanning: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isScanning) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "ButtonScale"
    )
    val color by animateColorAsState(
        targetValue = if (isScanning) Color.Red else NeonIndigo,
        label = "ButtonColor"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(100.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        // Outer Glow Effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color.copy(alpha = 0.2f), CircleShape)
        )
        // Main Inner Action Surface
        Surface(
            modifier = Modifier.size(70.dp),
            shape = CircleShape,
            color = color,
            shadowElevation = 16.dp
        ) {
            Icon(
                imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.CameraAlt,
                contentDescription = if (isScanning) "Stop Scan" else "Start Scan",
                tint = Color.White,
                modifier = Modifier.padding(20.dp)
            )
        }
    }
}
