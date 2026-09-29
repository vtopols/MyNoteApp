package com.example.note.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.note.utils.ThemeManager

enum class AppAccentColor(
    val color: Color,
    val container: Color,
    val displayName: String
) {
    BLUE(Color(0xFF2196F3), Color(0xFFBBDEFB), "Синий"),
    GREEN(Color(0xFF4CAF50), Color(0xFFC8E6C9), "Зелёный"),
    PURPLE(Color(0xFF9C27B0), Color(0xFFE1BEE7), "Фиолетовый"),
    RED(Color(0xFFF44336), Color(0xFFFFCDD2), "Красный"),
    ORANGE(Color(0xFFFF9800), Color(0xFFFFE0B2), "Оранжевый")
}

data class AppColorScheme(
    val primary: Color,           // Акцентный цвет (кнопки, иконки)
    val primaryContainer: Color,  // Светлый вариант акцента
    val secondary: Color,         // Вторичный акцент
    val secondaryContainer: Color,// Контейнер для списков (нейтральный!)
    val surface: Color,           // Фон карточек
    val onSurface: Color,         // Текст на карточках
    val onSurfaceVariant: Color,  // Второстепенный текст
    val error: Color,             // Цвет ошибок
    val background: Color         // Общий фон экрана
)

fun getColorScheme(accent: AppAccentColor, isDark: Boolean): AppColorScheme {
    return if (isDark) {
        AppColorScheme(
            primary = accent.color,
            primaryContainer = accent.color.copy(alpha = 0.2f),
            secondary = accent.color.copy(alpha = 0.8f),
            secondaryContainer = Color(0xFF2A2A2A),  // Нейтральный тёмный, не зависит от акцента
            surface = Color(0xFF1E1E1E),
            onSurface = Color(0xFFFFFFFF),
            onSurfaceVariant = Color(0xFFB0BEC5),
            error = Color(0xFFCF6679),
            background = Color(0xFF121212)
        )
    } else {
        AppColorScheme(
            primary = accent.color,
            primaryContainer = accent.container,
            secondary = accent.color.copy(alpha = 0.8f),
            secondaryContainer = Color(0xFFF0F0F0),  // Нейтральный светлый, не зависит от акцента
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF000000),
            onSurfaceVariant = Color(0xFF757575),
            error = Color(0xFFB00020),
            background = Color(0xFFF5F5F5)
        )
    }
}

val LocalAppColorScheme = staticCompositionLocalOf { getColorScheme(AppAccentColor.BLUE, false) }

val appTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    ThemeManager.init(context)

    val isDark = ThemeManager.isDark
    val accentColor = ThemeManager.accentColor
    val appColorScheme = getColorScheme(accentColor, isDark)

    CompositionLocalProvider(
        LocalAppColorScheme provides appColorScheme
    ) {
        androidx.compose.material3.MaterialTheme(
            colorScheme = if (isDark) {
                androidx.compose.material3.darkColorScheme()
            } else {
                androidx.compose.material3.lightColorScheme()
            },
            typography = appTypography,
            content = content
        )
    }
}