package com.dnscontrol.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4FC3F7),
    onPrimary = Color(0xFF003544),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFB8EAFF),
    secondary = Color(0xFF81C784),
    onSecondary = Color(0xFF003A00),
    secondaryContainer = Color(0xFF005300),
    onSecondaryContainer = Color(0xFFA5F5A0),
    tertiary = Color(0xFFFFB74D),
    onTertiary = Color(0xFF462A00),
    tertiaryContainer = Color(0xFF643F00),
    onTertiaryContainer = Color(0xFFFFDDB5),
    error = Color(0xFFEF5350),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF1A1A2E),
    onBackground = Color(0xFFE2E2E9),
    surface = Color(0xFF16213E),
    onSurface = Color(0xFFE2E2E9),
    surfaceVariant = Color(0xFF0F3460),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF8E9099)
)

@Composable
fun DNSControlTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
