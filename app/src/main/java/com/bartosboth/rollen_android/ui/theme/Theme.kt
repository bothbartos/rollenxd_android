package com.bartosboth.rollen_android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = BlueRibbon600,
    onPrimary = Color.White,
    primaryContainer = BlueRibbon100,
    onPrimaryContainer = BlueRibbon900,
    secondary = BlueRibbon400,
    onSecondary = Color.White,
    secondaryContainer = BlueRibbon50,
    onSecondaryContainer = BlueRibbon800,
    tertiary = RiverBed400,
    onTertiary = Color.White,
    tertiaryContainer = RiverBed100,
    onTertiaryContainer = RiverBed800,
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = RiverBed50,
    onBackground = RiverBed900,
    surface = Color.White,
    onSurface = RiverBed900,
    surfaceVariant = RiverBed100,
    onSurfaceVariant = RiverBed700,
    outline = RiverBed500
)

private val DarkColorScheme = darkColorScheme(
    primary = BlueRibbon400,
    onPrimary = BlueRibbon950,
    primaryContainer = BlueRibbon800,
    onPrimaryContainer = BlueRibbon100,
    secondary = BlueRibbon300,
    onSecondary = BlueRibbon900,
    secondaryContainer = BlueRibbon700,
    onSecondaryContainer = BlueRibbon100,
    tertiary = RiverBed300,
    onTertiary = RiverBed900,
    tertiaryContainer = RiverBed700,
    onTertiaryContainer = RiverBed100,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = RiverBed900,
    onBackground = RiverBed100,
    surface = RiverBed950,
    onSurface = RiverBed100,
    surfaceVariant = RiverBed800,
    onSurfaceVariant = RiverBed300,
    outline = RiverBed400
)

@Composable
fun Rollen_androidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
