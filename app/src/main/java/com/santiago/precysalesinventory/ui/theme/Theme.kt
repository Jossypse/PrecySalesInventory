package com.santiago.precysalesinventory.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = DarkBlue,
    onPrimary = Color.White,
    secondary = LightBlue,
    onSecondary = Color.White,
    tertiary = SuccessGreen,
    onTertiary = Color.White,
    background = OffWhite,
    onBackground = TextBlack,
    surface = Color.White,
    onSurface = TextBlack,
    surfaceVariant = SoftGray,
    onSurfaceVariant = TextBlack,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun PrecySalesInventoryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
