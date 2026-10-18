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

// Calm Editorial SaaS color scheme from vend-pixel/kungacv
private val KungaLightColorScheme = lightColorScheme(
    primary = Brand600,
    onPrimary = Color.White,
    primaryContainer = Brand50,
    onPrimaryContainer = Brand800,
    secondary = Ink950,
    onSecondary = Color.White,
    secondaryContainer = Ink100,
    onSecondaryContainer = Ink950,
    tertiary = Brand700,
    onTertiary = Color.White,
    tertiaryContainer = Brand100,
    onTertiaryContainer = Brand900,
    background = WarmPaperBg,
    onBackground = Ink950,
    surface = CrispPaperWhite,
    onSurface = Ink950,
    surfaceVariant = WarmInputSurface,
    onSurfaceVariant = Ink500,
    outline = WarmBorder,
    outlineVariant = BrandTintBorder,
    error = CrimsonError,
    onError = Color.White,
    errorContainer = CrimsonErrorBg,
    onErrorContainer = CrimsonError
)

private val KungaDarkColorScheme = darkColorScheme(
    primary = Brand500,
    onPrimary = Color.White,
    primaryContainer = Ink800,
    onPrimaryContainer = Brand300,
    secondary = Brand400,
    onSecondary = Ink950,
    secondaryContainer = Ink800,
    onSecondaryContainer = Ink50,
    tertiary = Brand300,
    onTertiary = Ink950,
    tertiaryContainer = Ink850,
    onTertiaryContainer = Brand200,
    background = Ink950,
    onBackground = Ink50,
    surface = Ink900,
    onSurface = Ink50,
    surfaceVariant = Ink850,
    onSurfaceVariant = Ink400,
    outline = Ink700,
    outlineVariant = Ink800,
    error = Color(0xFFF87171),
    onError = Ink950
)

val KungaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(11.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun KungaCVTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) KungaDarkColorScheme else KungaLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = KungaShapes,
        content = content
    )
}
