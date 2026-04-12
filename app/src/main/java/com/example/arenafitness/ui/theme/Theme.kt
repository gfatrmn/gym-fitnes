package com.example.arenafitness.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryRed,
    background = DarkBackground,
    surface = DarkBackground,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun ArenaFitnessTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}