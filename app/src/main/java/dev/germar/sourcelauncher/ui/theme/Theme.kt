package dev.germar.sourcelauncher.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Typography
import androidx.tv.material3.darkColorScheme

object LauncherColors {
    val BackgroundTop = Color(0xFF15161B)
    val BackgroundBottom = Color(0xFF050507)
    val TextPrimary = Color(0xFFF5F5F7)
    val TextSecondary = Color(0xFFF5F5F7).copy(alpha = 0.62f)
    val TextTertiary = Color(0xFFF5F5F7).copy(alpha = 0.4f)
    val RowFocused = Color(0xFFF5F5F7)
    val RowFocusedText = Color(0xFF111114)
    val RowIdle = Color.White.copy(alpha = 0.06f)
    val Accent = Color(0xFF0A84FF)
    val Sheet = Color(0xFF1E1F24)
}

private val typography = Typography(
    displaySmall = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.4).sp),
    headlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold),
    titleLarge = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium),
    titleMedium = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
)

@Composable
fun LauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = LauncherColors.TextPrimary,
            onPrimary = LauncherColors.RowFocusedText,
            background = LauncherColors.BackgroundBottom,
            surface = LauncherColors.BackgroundTop,
            onSurface = LauncherColors.TextPrimary,
            onBackground = LauncherColors.TextPrimary,
        ),
        typography = typography,
        content = content,
    )
}
