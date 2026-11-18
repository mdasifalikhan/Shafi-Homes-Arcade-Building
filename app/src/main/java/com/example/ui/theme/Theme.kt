package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColorScheme = lightColorScheme(
    primary = SlateTeal40,
    onPrimary = Color.White,
    primaryContainer = SlateTealContainer,
    onPrimaryContainer = OnSlateTealContainer,
    secondary = Terracotta40,
    onSecondary = Color.White,
    secondaryContainer = TerracottaContainer,
    onSecondaryContainer = OnTerracottaContainer,
    tertiary = Sage40,
    onTertiary = Color.White,
    tertiaryContainer = SageContainer,
    onTertiaryContainer = OnSageContainer,
    background = AlabasterBg,
    onBackground = InkDark,
    surface = StoneSurface,
    onSurface = InkDark,
    surfaceVariant = StoneSurfaceVariant,
    onSurfaceVariant = InkMuted,
    outline = OutlineStone,
    error = StatusOverdueRed,
    onError = Color.White,
    errorContainer = StatusOverdueBg,
    onErrorContainer = StatusOverdueRed
)

private val DarkColorScheme = darkColorScheme(
    primary = SlateTeal80,
    onPrimary = Color(0xFF07252B),
    primaryContainer = Color(0xFF163D45),
    onPrimaryContainer = SlateTealContainer,
    secondary = Terracotta80,
    onSecondary = Color(0xFF441608),
    secondaryContainer = Color(0xFF5D2513),
    onSecondaryContainer = TerracottaContainer,
    tertiary = Sage80,
    onTertiary = Color(0xFF082E1E),
    tertiaryContainer = Color(0xFF1A4A35),
    onTertiaryContainer = SageContainer,
    background = ObsidianBg,
    onBackground = IvoryText,
    surface = ObsidianSurface,
    onSurface = IvoryText,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = SlateMutedText,
    outline = OutlineDark,
    error = Color(0xFFFF8A8A),
    onError = Color(0xFF490B0B),
    errorContainer = Color(0xFF641414),
    onErrorContainer = Color(0xFFFFDAD6)
)

val CivicNestShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun CivicNestTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CivicNestTypography,
        shapes = CivicNestShapes,
        content = content
    )
}
