package com.example.guscard.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Цвета в стиле iOS (System Colors)
private val IOSBlue = Color(0xFF007AFF)
private val IOSRed = Color(0xFFFF3B30)
private val IOSGreen = Color(0xFF34C759)
private val IOSGray = Color(0xFF8E8E93)

private val LightColors = lightColorScheme(
    primary = IOSBlue,
    onPrimary = Color.White,
    secondary = IOSGray,
    background = Color(0xFFF2F2F7), // Светло-серый фон iOS
    surface = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF0A84FF), // Синий для темной темы
    onPrimary = Color.White,
    secondary = IOSGray,
    background = Color.Black,
    surface = Color(0xFF1C1C1E), // Темно-серый фон iOS
    onBackground = Color.White,
    onSurface = Color.White
)

// Глобальные формы для iOS-стиля
private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun GusCardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Отключаем динамические цвета, чтобы сохранить iOS-стиль
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Динамические цвета Android 12+ (если захотите включить, поставьте true выше)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = AppShapes,
        content = content
    )
}