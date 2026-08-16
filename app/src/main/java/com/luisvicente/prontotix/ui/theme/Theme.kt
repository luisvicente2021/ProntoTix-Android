package com.luisvicente.prontotix.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ProntoTixColors = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,

    primaryContainer = Color(0xFFEAF2FF),
    onPrimaryContainer = Color(0xFF0D1B2A),

    secondary = Color(0xFF10B981),
    onSecondary = Color.White,

    secondaryContainer = Color(0xFFE7F8F2),
    onSecondaryContainer = Color(0xFF064E3B),

    tertiary = Color(0xFF06B6D4),
    onTertiary = Color.White,

    background = Color(0xFFF5F7FA),
    onBackground = Color(0xFF111827),

    surface = Color.White,
    onSurface = Color(0xFF111827),

    surfaceVariant = Color(0xFFF5F7FA),
    onSurfaceVariant = Color(0xFF4B5563),

    error = Color(0xFFDC2626),
    onError = Color.White,

    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF991B1B),

    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFE5E7EB)
)

@Composable
fun ProntoTixTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = ProntoTixColors,
        content = content
    )
}