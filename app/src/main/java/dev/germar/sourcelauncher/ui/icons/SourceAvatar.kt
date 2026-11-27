package dev.germar.sourcelauncher.ui.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import dev.germar.sourcelauncher.model.Appearance
import dev.germar.sourcelauncher.model.Glyph
import dev.germar.sourcelauncher.model.Palette

val Palette.topColor: Color get() = Color(top)
val Palette.bottomColor: Color get() = Color(bottom)

/** A round, Apple-TV-profile-style avatar: a gradient disc with a white glyph or initials. */
@Composable
fun SourceAvatar(
    appearance: Appearance,
    name: String,
    size: Dp,
    modifier: Modifier = Modifier,
    dimmed: Boolean = false,
) {
    val top = appearance.palette.topColor
    val bottom = appearance.palette.bottomColor
    Box(
        modifier = modifier
            .size(size)
            .alpha(if (dimmed) 0.38f else 1f)
            .drawBehind {
                val radius = this.size.minDimension / 2f
                drawCircle(Brush.verticalGradient(listOf(top, bottom)), radius)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                        center = Offset(this.size.width * 0.32f, this.size.height * 0.12f),
                        radius = radius * 1.25f,
                    ),
                    radius = radius,
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        val vector = SourceGlyphs.vector(appearance.glyph)
        if (appearance.glyph == Glyph.MONOGRAM || vector == null) {
            Text(
                text = remember(name) { initialsOf(name) },
                color = Color.White,
                fontSize = (size.value * 0.36f).sp,
                fontWeight = FontWeight.SemiBold,
            )
        } else {
            Image(
                painter = rememberVectorPainter(vector),
                contentDescription = null,
                modifier = Modifier.size(size * 0.6f),
            )
        }
    }
}

fun initialsOf(name: String): String {
    val words = name.split(Regex("\\s+")).filter { it.isNotBlank() }
    return when {
        words.isEmpty() -> "?"
        words.size == 1 -> words[0].take(2).replaceFirstChar { it.uppercase() }
        else -> "${words[0].first()}${words[1].first()}".uppercase()
    }
}
