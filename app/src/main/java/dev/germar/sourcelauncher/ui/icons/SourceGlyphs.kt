package dev.germar.sourcelauncher.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.unit.dp
import dev.germar.sourcelauncher.model.Glyph
import kotlin.math.cos
import kotlin.math.sin

/**
 * The launcher's own icon pack. Every glyph is original artwork drawn from simple geometric
 * primitives on a 48×48 grid: generic device silhouettes, no brand logos.
 */
object SourceGlyphs {

    private val cache = mutableMapOf<Glyph, ImageVector?>()

    /** Returns the vector for [glyph], or null for [Glyph.MONOGRAM], which is rendered as text. */
    fun vector(glyph: Glyph): ImageVector? = cache.getOrPut(glyph) { build(glyph) }

    private fun build(glyph: Glyph): ImageVector? = when (glyph) {
        Glyph.CONSOLE_TOWER -> consoleTower()
        Glyph.CONSOLE_SLAB -> consoleSlab()
        Glyph.CONSOLE_BOX -> consoleBox()
        Glyph.CONSOLE_COMPACT -> consoleCompact()
        Glyph.CONSOLE_FLAT -> consoleFlat()
        Glyph.CONSOLE_RETRO -> consoleRetro()
        Glyph.HYBRID_CONSOLE -> hybridConsole()
        Glyph.HANDHELD_PC -> handheldPc()
        Glyph.GAMEPAD -> gamepad()
        Glyph.STREAM_BOX -> streamBox()
        Glyph.STREAM_STICK -> streamStick()
        Glyph.STREAM_PUCK -> streamPuck()
        Glyph.SET_TOP_BOX -> setTopBox()
        Glyph.SATELLITE -> satellite()
        Glyph.DISC -> disc()
        Glyph.RECEIVER -> receiver()
        Glyph.DESKTOP -> desktop()
        Glyph.LAPTOP -> laptop()
        Glyph.MINI_PC -> miniPc()
        Glyph.BOARD -> board()
        Glyph.CAMERA -> camera()
        Glyph.PHONE -> phone()
        Glyph.ANTENNA -> antenna()
        Glyph.HDMI -> hdmi()
        Glyph.AV -> av()
        Glyph.PLAY -> play()
        Glyph.STAR -> star()
        Glyph.MONOGRAM -> null
        Glyph.CONSOLE_SLAB_TRIPLE -> consoleSlabTriple()
        Glyph.CONSOLE_GROOVED -> consoleGrooved()
        Glyph.CONSOLE_CURVED -> consoleCurved()
        Glyph.CONSOLE_CONCAVE -> consoleConcave()
        Glyph.CONSOLE_TWO_TONE -> consoleTwoTone()
        Glyph.CONSOLE_PERFORATED -> consolePerforated()
        Glyph.CONSOLE_SLIM -> consoleSlim()
        Glyph.CONSOLE_CUBE -> consoleCube()
        Glyph.GAMEPAD_SYMMETRIC -> gamepadSymmetric()
        Glyph.GAMEPAD_TABLET -> gamepadTablet()
        Glyph.GAMEPAD_CLASSIC -> gamepadClassic()
        Glyph.HANDHELD_TRACKPADS -> handheldTrackpads()
        Glyph.HANDHELD_ANGULAR -> handheldAngular()
        Glyph.HANDHELD_DETACHABLE -> handheldDetachable()
        Glyph.HANDHELD_GRIPS -> handheldGrips()
        Glyph.SOUNDBAR -> soundbar()
        Glyph.TABLET -> tablet()
    }

    private fun consoleTower() = glyph("console_tower") {
        shadow(24f, 45.2f, 11f)
        fill {
            moveTo(22.6f, 4.5f); lineTo(22.6f, 43.5f); lineTo(17.2f, 43.5f)
            curveTo(16.0f, 43.5f, 15.3f, 42.7f, 15.4f, 41.5f)
            curveTo(16.4f, 31f, 16.2f, 20f, 13.6f, 8.2f)
            curveTo(13.3f, 6.5f, 14.2f, 5.0f, 16.0f, 4.8f)
            close()
        }
        fill {
            moveTo(25.4f, 4.5f); lineTo(25.4f, 43.5f); lineTo(30.8f, 43.5f)
            curveTo(32.0f, 43.5f, 32.7f, 42.7f, 32.6f, 41.5f)
            curveTo(31.6f, 31f, 31.8f, 20f, 34.4f, 8.2f)
            curveTo(34.7f, 6.5f, 33.8f, 5.0f, 32.0f, 4.8f)
            close()
        }
        fill(alpha = 0.5f) { roundRect(23.1f, 7f, 24.9f, 42f, 0.9f) }
    }

    private fun consoleSlab() = glyph("console_slab") {
        shadow(23.5f, 37.6f, 17f)
        fill {
            polygon(10.5f, 14.5f, 42f, 14.5f, 39.6f, 24f, 8.1f, 24f)
            polygon(13.4f, 18.6f, 33f, 18.6f, 32.6f, 20.1f, 13f, 20.1f)
        }
        fill(alpha = 0.8f) { polygon(7.7f, 25.6f, 39.2f, 25.6f, 37f, 34f, 5.5f, 34f) }
    }

    private fun consoleBox() = glyph("console_box") {
        shadow(26f, 44.6f, 12.5f)
        fill(alpha = 0.6f) { polygon(33f, 12f, 37f, 7.5f, 37f, 38.5f, 33f, 43f) }
        fill(alpha = 0.82f) {
            polygon(15f, 12f, 19f, 7.5f, 37f, 7.5f, 33f, 12f)
            ellipse(26f, 9.75f, 6f, 1.5f)
        }
        fill {
            roundRect(15f, 12f, 33f, 43f, 1.2f)
            circle(18.2f, 15.6f, 0.95f)
            roundRect(17.6f, 19.5f, 18.7f, 34f, 0.5f)
        }
    }

    private fun consoleCompact() = glyph("console_compact") {
        shadow(24f, 36.6f, 17f)
        fill {
            roundRect(6f, 15.5f, 42f, 33.5f, 2.5f)
            circle(31.5f, 24.5f, 6.4f)
            circle(10.2f, 19.6f, 0.95f)
        }
        fill(alpha = 0.55f) {
            circle(31.5f, 24.5f, 5f)
            circle(31.5f, 24.5f, 3.5f)
        }
    }

    private fun consoleFlat() = glyph("console_flat") {
        shadow(24f, 35.4f, 17f)
        fill(alpha = 0.72f) { polygon(8.5f, 17f, 39.5f, 17f, 43.5f, 22f, 4.5f, 22f) }
        fill {
            roundRect(4.5f, 22f, 43.5f, 31.5f, 1.2f)
            roundRect(8f, 25.4f, 22f, 26.6f, 0.6f)
            circle(39.2f, 26f, 1.2f)
            roundRect(34.6f, 25.5f, 36.2f, 26.5f, 0.3f)
        }
        fill(alpha = 0.45f) { rect(8f, 31.5f, 40f, 33.6f) }
    }

    private fun consoleRetro() = glyph("console_retro") {
        shadow(24f, 38f, 17f)
        fill(alpha = 0.8f) {
            roundRect(15f, 8.5f, 33f, 22f, 1.2f)
            roundRect(17.5f, 11f, 30.5f, 16.5f, 0.8f)
        }
        fill {
            roundRect(6f, 20f, 42f, 35f, 3.5f)
            circle(12.5f, 27.5f, 1.9f)
            circle(18f, 27.5f, 1.9f)
            roundRect(27.5f, 25f, 37f, 26.1f, 0.55f)
            roundRect(27.5f, 28.4f, 37f, 29.5f, 0.55f)
        }
    }

    private fun hybridConsole() = glyph("hybrid_console") {
        fill(alpha = 0.9f) {
            moveTo(8.9f, 14.5f); lineTo(8.9f, 33.5f); lineTo(6.5f, 33.5f)
            curveTo(4.3f, 33.5f, 2.5f, 31.7f, 2.5f, 29.5f); lineTo(2.5f, 18.5f)
            curveTo(2.5f, 16.3f, 4.3f, 14.5f, 6.5f, 14.5f); close()
            circle(5.8f, 19.2f, 1.7f)
            circle(5.8f, 25.3f, 0.7f); circle(5.8f, 28.5f, 0.7f)
            circle(4.2f, 26.9f, 0.7f); circle(7.4f, 26.9f, 0.7f)
        }
        fill(alpha = 0.9f) {
            moveTo(39.1f, 14.5f); lineTo(39.1f, 33.5f); lineTo(41.5f, 33.5f)
            curveTo(43.7f, 33.5f, 45.5f, 31.7f, 45.5f, 29.5f); lineTo(45.5f, 18.5f)
            curveTo(45.5f, 16.3f, 43.7f, 14.5f, 41.5f, 14.5f); close()
            circle(42.2f, 17.6f, 0.7f); circle(42.2f, 20.8f, 0.7f)
            circle(40.6f, 19.2f, 0.7f); circle(43.8f, 19.2f, 0.7f)
            circle(42.2f, 26.9f, 1.7f)
        }
        fill {
            roundRect(9.6f, 14.5f, 38.4f, 33.5f, 1f)
            roundRect(11f, 15.9f, 37f, 32.1f, 0.5f)
        }
        fill(alpha = 0.28f) { roundRect(11f, 15.9f, 37f, 32.1f, 0.5f) }
    }

    private fun handheldPc() = glyph("handheld_pc") {
        fill {
            roundRect(3.5f, 14.5f, 44.5f, 33.5f, 8f)
            roundRect(14f, 16.8f, 34f, 31.2f, 1f)
            circle(8.9f, 19.9f, 2.1f)
            roundRect(6.7f, 25.2f, 11.1f, 29.6f, 0.9f)
            circle(39.1f, 17.9f, 0.8f); circle(39.1f, 21.5f, 0.8f)
            circle(37.3f, 19.7f, 0.8f); circle(40.9f, 19.7f, 0.8f)
            roundRect(36.9f, 25.2f, 41.3f, 29.6f, 0.9f)
        }
        fill(alpha = 0.28f) { roundRect(14f, 16.8f, 34f, 31.2f, 1f) }
    }

    private fun gamepad() = glyph("gamepad") {
        fill {
            moveTo(15f, 13.5f); lineTo(33f, 13.5f)
            curveTo(38.6f, 13.5f, 41.2f, 16.8f, 42.4f, 22.4f)
            lineTo(44.2f, 31f)
            curveTo(45f, 35.2f, 41.6f, 38.4f, 38.6f, 36.2f)
            lineTo(33.4f, 31.2f); lineTo(14.6f, 31.2f); lineTo(9.4f, 36.2f)
            curveTo(6.4f, 38.4f, 3f, 35.2f, 3.8f, 31f)
            lineTo(5.6f, 22.4f)
            curveTo(6.8f, 16.8f, 9.4f, 13.5f, 15f, 13.5f)
            close()
            circle(12.8f, 20.6f, 2.9f)
            dpad(18.8f, 26.6f, arm = 3f, half = 1f)
            circle(35.2f, 18.3f, 1.25f); circle(37.8f, 20.9f, 1.25f)
            circle(35.2f, 23.5f, 1.25f); circle(32.6f, 20.9f, 1.25f)
            circle(29.2f, 26.6f, 2.5f)
            circle(24f, 17.4f, 1.4f)
        }
        fill { circle(12.8f, 20.6f, 1.9f) }
        fill { circle(29.2f, 26.6f, 1.6f) }
    }

    private fun streamBox() = glyph("stream_box") {
        shadow(17.5f, 34.6f, 13.5f)
        fill(alpha = 0.68f) {
            roundRect(4f, 21f, 31f, 32.5f, 5f)
            circle(27.4f, 30.4f, 0.65f)
        }
        fill { roundRect(4f, 17f, 31f, 28.5f, 5f) }
        fill {
            roundRect(35f, 9f, 41.5f, 39f, 3.25f)
            circle(38.25f, 14.4f, 2.3f)
            circle(38.25f, 20.8f, 0.9f)
            circle(38.25f, 24.2f, 0.9f)
        }
    }

    private fun streamStick() = glyph("stream_stick") {
        fill(alpha = 0.75f) {
            rect(29.5f, 21.8f, 35.5f, 27.7f)
            rect(30.9f, 23.5f, 35.5f, 26f)
        }
        fill {
            roundRect(4.5f, 19.8f, 30f, 29.7f, 3f)
            circle(8.4f, 24.75f, 0.85f)
        }
        fill {
            roundRect(38.5f, 10f, 44f, 39f, 2.75f)
            circle(41.25f, 15f, 1.9f)
            circle(41.25f, 21.4f, 0.8f)
            circle(41.25f, 24.8f, 0.8f)
        }
    }

    private fun streamPuck() = glyph("stream_puck") {
        stroke(width = 2.4f, alpha = 0.85f) {
            moveTo(29f, 27.5f); curveTo(33f, 33.5f, 36f, 38f, 40.5f, 38f)
        }
        fill(alpha = 0.85f) { roundRect(39.5f, 35.3f, 45f, 40.7f, 1f) }
        fill {
            ellipse(19f, 21f, 13.5f, 9.5f)
            circle(19f, 26.6f, 0.75f)
        }
    }

    private fun setTopBox() = glyph("set_top_box") {
        fill(alpha = 0.7f) {
            polygon(8f, 14.5f, 40f, 14.5f, 44f, 19f, 4f, 19f)
            rect(8f, 31f, 12f, 33.2f)
        }
        fill(alpha = 0.7f) { rect(36f, 31f, 40f, 33.2f) }
        fill {
            roundRect(4f, 19f, 44f, 31f, 1.6f)
            roundRect(8f, 22.5f, 20f, 27.5f, 1f)
            circle(35.5f, 25f, 1.1f)
            circle(39.5f, 25f, 1.1f)
        }
        fill(alpha = 0.85f) {
            rect(9.6f, 24f, 11.6f, 26f)
            rect(12.6f, 24f, 14.6f, 26f)
            rect(16.2f, 24f, 18.2f, 26f)
        }
    }

    private fun satellite() = glyph("satellite") {
        stroke(width = 2.2f) {
            moveTo(20f, 32f); lineTo(18.2f, 42.6f)
            moveTo(12.5f, 43f); lineTo(24f, 43f)
        }
        fill {
            moveTo(8.5f, 11f)
            curveTo(6.5f, 24f, 18f, 36.5f, 33.5f, 37.5f)
            close()
        }
        stroke(width = 1.8f) { moveTo(20.5f, 24.5f); lineTo(31.5f, 13.5f) }
        fill { circle(33.2f, 11.8f, 2.6f) }
        stroke(width = 1.6f, alpha = 0.7f) {
            moveTo(37.2f, 6.2f); curveTo(39.4f, 6.6f, 41f, 8.3f, 41.4f, 10.5f)
            moveTo(37.6f, 2.4f); curveTo(41.9f, 3f, 44.6f, 6f, 45.2f, 10.1f)
        }
    }

    private fun disc() = glyph("disc") {
        fill {
            circle(24f, 24f, 16f)
            circle(24f, 24f, 3.4f)
        }
        fill(alpha = 0.18f, color = Color.Black) {
            circle(24f, 24f, 7f)
            circle(24f, 24f, 3.4f)
        }
        stroke(width = 1.6f, alpha = 0.22f, color = Color.Black) {
            moveTo(13.5f, 16.5f); curveTo(15.5f, 13.4f, 18.6f, 11.2f, 22.2f, 10.4f)
            moveTo(34.5f, 31.5f); curveTo(32.5f, 34.6f, 29.4f, 36.8f, 25.8f, 37.6f)
        }
    }

    private fun receiver() = glyph("receiver") {
        fill(alpha = 0.7f) {
            rect(8f, 32.5f, 12f, 34.6f)
            rect(36f, 32.5f, 40f, 34.6f)
        }
        fill {
            roundRect(4f, 15.5f, 44f, 32.5f, 2f)
            roundRect(8f, 19.5f, 24f, 24f, 0.9f)
            circle(35.5f, 24f, 5.2f)
            circle(9f, 28.4f, 0.9f); circle(12.5f, 28.4f, 0.9f)
            circle(16f, 28.4f, 0.9f); circle(19.5f, 28.4f, 0.9f)
        }
        fill(alpha = 0.85f) { circle(35.5f, 24f, 3.6f) }
    }

    private fun desktop() = glyph("desktop") {
        fill {
            roundRect(5f, 8f, 43f, 33f, 2.2f)
            rect(7.6f, 10.6f, 40.4f, 30.4f)
        }
        fill(alpha = 0.3f) { rect(7.6f, 10.6f, 40.4f, 30.4f) }
        fill(alpha = 0.8f) { polygon(21f, 33f, 27f, 33f, 28f, 38.5f, 20f, 38.5f) }
        fill { roundRect(14.5f, 38.5f, 33.5f, 41f, 1.25f) }
    }

    private fun laptop() = glyph("laptop") {
        fill {
            roundRect(9.5f, 9.5f, 38.5f, 31f, 1.8f)
            roundRect(11.8f, 11.8f, 36.2f, 28.8f, 0.6f)
        }
        fill(alpha = 0.3f) { roundRect(11.8f, 11.8f, 36.2f, 28.8f, 0.6f) }
        fill {
            moveTo(3.5f, 32.2f); lineTo(44.5f, 32.2f); lineTo(44.5f, 33.3f)
            curveTo(44.5f, 35f, 43.3f, 36f, 41.6f, 36f); lineTo(6.4f, 36f)
            curveTo(4.7f, 36f, 3.5f, 35f, 3.5f, 33.3f); close()
            roundRect(20.5f, 32.2f, 27.5f, 33.6f, 0.6f)
        }
    }

    private fun miniPc() = glyph("mini_pc") {
        shadow(24.5f, 42.6f, 15f)
        fill(alpha = 0.6f) { polygon(33f, 17f, 40f, 10f, 40f, 34f, 33f, 41f) }
        fill(alpha = 0.8f) { polygon(9f, 17f, 16f, 10f, 40f, 10f, 33f, 17f) }
        fill {
            roundRect(9f, 17f, 33f, 41f, 1.5f)
            roundRect(12f, 36f, 30f, 37.2f, 0.6f)
        }
    }

    private fun board() = glyph("board") {
        fill(alpha = 0.85f) {
            rect(38f, 16f, 44f, 22.5f)
            rect(38f, 25f, 44f, 31.5f)
        }
        fill {
            roundRect(6f, 10f, 40f, 38f, 2.2f)
            circle(9.2f, 13.2f, 1.3f); circle(36.8f, 13.2f, 1.3f)
            circle(9.2f, 34.8f, 1.3f); circle(36.8f, 34.8f, 1.3f)
            roundRect(14f, 18f, 27f, 31f, 1.2f)
            for (i in 0 until 9) circle(14f + i * 2.4f, 13.2f, 0.65f)
        }
        fill(alpha = 0.9f) { roundRect(15.6f, 19.6f, 25.4f, 29.4f, 0.8f) }
    }

    private fun camera() = glyph("camera") {
        fill { polygon(16f, 15.6f, 19.2f, 10.5f, 28.8f, 10.5f, 32f, 15.6f) }
        fill {
            roundRect(5f, 15f, 43f, 38f, 4f)
            circle(24f, 26.5f, 8.2f)
            circle(37.2f, 20f, 1.3f)
        }
        fill {
            circle(24f, 26.5f, 6.4f)
            circle(24f, 26.5f, 3.4f)
        }
        fill(alpha = 0.9f) { circle(22.6f, 25.1f, 1.1f) }
    }

    private fun phone() = glyph("phone") {
        fill {
            roundRect(15f, 5f, 33f, 43f, 4f)
            roundRect(17f, 9f, 31f, 39f, 1.2f)
        }
        fill(alpha = 0.3f) { roundRect(17f, 9f, 31f, 39f, 1.2f) }
        fill(alpha = 0.6f, color = Color.Black) { roundRect(21.5f, 6.3f, 26.5f, 7.6f, 0.65f) }
    }

    private fun antenna() = glyph("antenna") {
        stroke(width = 1.8f) {
            moveTo(24f, 16.5f); lineTo(16.5f, 6.5f)
            moveTo(24f, 16.5f); lineTo(31.5f, 6.5f)
        }
        fill { circle(16.3f, 6.2f, 1.4f) }
        fill { circle(31.7f, 6.2f, 1.4f) }
        fill {
            roundRect(6f, 16.5f, 42f, 40f, 3f)
            roundRect(9f, 19.5f, 39f, 37f, 1.6f)
        }
        fill(alpha = 0.3f) { roundRect(9f, 19.5f, 39f, 37f, 1.6f) }
    }

    private fun hdmi() = glyph("hdmi") {
        stroke(width = 5f, alpha = 0.85f) { moveTo(24f, 9f); lineTo(24f, 3f) }
        fill(alpha = 0.85f) {
            roundRect(15f, 8.5f, 33f, 22.5f, 2.5f)
            roundRect(18f, 12.3f, 30f, 13.3f, 0.5f)
            roundRect(18f, 15.3f, 30f, 16.3f, 0.5f)
        }
        fill {
            polygon(12.5f, 22f, 35.5f, 22f, 35.5f, 30.5f, 31.5f, 35.5f, 16.5f, 35.5f, 12.5f, 30.5f)
            polygon(15f, 24.5f, 33f, 24.5f, 33f, 29.6f, 30.4f, 33f, 17.6f, 33f, 15f, 29.6f)
        }
        fill(alpha = 0.85f) { rect(17.2f, 26.6f, 30.8f, 28.8f) }
    }

    private fun av() = glyph("av") {
        listOf(13f to 1f, 24f to 0.85f, 35f to 0.7f).forEach { (x, alpha) ->
            stroke(width = 2.6f, alpha = alpha) { moveTo(x, 32f); lineTo(x, 42.5f) }
            fill(alpha = alpha) { rect(x - 1.1f, 8f, x + 1.1f, 15f) }
            fill(alpha = alpha * 0.85f) { roundRect(x - 3.2f, 14.5f, x + 3.2f, 19f, 1f) }
            fill(alpha = alpha) {
                roundRect(x - 3.8f, 18.5f, x + 3.8f, 32f, 1.8f)
                roundRect(x - 2.6f, 22.5f, x + 2.6f, 23.3f, 0.4f)
                roundRect(x - 2.6f, 25.5f, x + 2.6f, 26.3f, 0.4f)
            }
        }
    }

    private fun play() = glyph("play") {
        fill {
            roundRect(8f, 10f, 40f, 38f, 7f)
            polygon(20.5f, 17.5f, 31.5f, 24f, 20.5f, 30.5f)
        }
    }

    private fun star() = glyph("star") {
        val points = FloatArray(20)
        for (i in 0 until 10) {
            val radius = if (i % 2 == 0) 17f else 7.4f
            val angle = Math.toRadians(-90.0 + i * 36.0)
            points[i * 2] = 24f + (radius * cos(angle)).toFloat()
            points[i * 2 + 1] = 25.5f + (radius * sin(angle)).toFloat()
        }
        fill { polygon(*points) }
        stroke(width = 2f) { polygon(*points) }
    }

    private fun consoleSlabTriple() = glyph("console_slab_triple") {
        shadow(24f, 36.4f, 17f)
        fill { polygon(11.5f, 10.5f, 43f, 10.5f, 41.3f, 17.2f, 9.8f, 17.2f) }
        fill(alpha = 0.88f) {
            polygon(9.5f, 18.4f, 41f, 18.4f, 39.3f, 25.1f, 7.8f, 25.1f)
            polygon(12.6f, 21.2f, 32.5f, 21.2f, 32.2f, 22.3f, 12.3f, 22.3f)
        }
        fill(alpha = 0.76f) { polygon(7.5f, 26.3f, 39f, 26.3f, 37.3f, 33f, 5.8f, 33f) }
    }

    private fun consoleGrooved() = glyph("console_grooved") {
        shadow(24f, 34.6f, 18.5f)
        fill(alpha = 0.72f) {
            polygon(8.5f, 13.5f, 39.5f, 13.5f, 44f, 19f, 4f, 19f)
            polygon(15.2f, 14.6f, 40.4f, 14.6f, 40.8f, 15.1f, 14.9f, 15.1f)
            polygon(14.4f, 16.1f, 41.6f, 16.1f, 42f, 16.6f, 14.1f, 16.6f)
            polygon(13.6f, 17.6f, 42.8f, 17.6f, 43.2f, 18.1f, 13.3f, 18.1f)
        }
        fill { roundRect(4f, 19f, 10.6f, 32.5f, 0.8f) }
        fill {
            roundRect(11.4f, 19f, 44f, 32.5f, 0.8f)
            rect(14f, 22.6f, 41f, 23.5f)
            roundRect(14.5f, 26.6f, 18.7f, 29.6f, 0.5f)
            rect(20.2f, 27.3f, 24.4f, 28.9f)
            roundRect(26.3f, 26.6f, 30.5f, 29.6f, 0.5f)
            rect(32f, 27.3f, 36.2f, 28.9f)
            circle(40.2f, 28.1f, 1f)
        }
    }

    private fun consoleCurved() = glyph("console_curved") {
        shadow(24f, 35.8f, 18f)
        fill {
            moveTo(5f, 26.5f)
            curveTo(5f, 19f, 13f, 14f, 24f, 14f)
            curveTo(35f, 14f, 43f, 19f, 43f, 26.5f)
            lineTo(43f, 28.6f); lineTo(5f, 28.6f); close()
            roundRect(13f, 22.4f, 35f, 23.6f, 0.6f)
            circle(38.6f, 25.4f, 0.8f)
        }
        fill(alpha = 0.7f) { roundRect(5f, 29.4f, 43f, 34f, 1.2f) }
    }

    private fun consoleConcave() = glyph("console_concave") {
        shadow(24f, 35.6f, 18f)
        fill {
            moveTo(6.5f, 15f)
            curveTo(17f, 18.2f, 31f, 18.2f, 41.5f, 15f)
            curveTo(42.7f, 14.6f, 43.5f, 15.4f, 43.5f, 16.6f)
            lineTo(43.5f, 31.4f)
            curveTo(43.5f, 32.6f, 42.7f, 33.4f, 41.5f, 33f)
            curveTo(31f, 29.8f, 17f, 29.8f, 6.5f, 33f)
            curveTo(5.3f, 33.4f, 4.5f, 32.6f, 4.5f, 31.4f)
            lineTo(4.5f, 16.6f)
            curveTo(4.5f, 15.4f, 5.3f, 14.6f, 6.5f, 15f)
            close()
            roundRect(8.5f, 23.4f, 30f, 24.6f, 0.6f)
            rect(32.6f, 18f, 33.3f, 30f)
            circle(37.8f, 24f, 2.6f)
            circle(37.8f, 24f, 1.6f)
        }
    }

    private fun consoleTwoTone() = glyph("console_two_tone") {
        shadow(24f, 35f, 18.5f)
        fill(alpha = 0.85f) { polygon(9.5f, 15.5f, 24f, 15.5f, 23.4f, 21f, 5f, 21f) }
        fill(alpha = 0.6f) {
            polygon(24.8f, 15.5f, 38.5f, 15.5f, 43f, 21f, 24.2f, 21f)
            rect(25.8f, 16.8f, 39.6f, 17.4f)
            rect(25.6f, 18.4f, 40.9f, 19f)
        }
        fill {
            rect(5f, 21f, 23.6f, 33f)
            roundRect(8.5f, 26.4f, 20f, 27.6f, 0.6f)
        }
        fill(alpha = 0.82f) {
            rect(24.4f, 21f, 43f, 33f)
            circle(38f, 27f, 1.5f)
        }
    }

    private fun consolePerforated() = glyph("console_perforated") {
        shadow(24f, 35f, 16f)
        fill(alpha = 0.72f) {
            polygon(10f, 16f, 38f, 16f, 42f, 21f, 6f, 21f)
            for (row in 0 until 3) {
                val y = 17.2f + row * 1.4f
                for (col in 0 until 6) circle(28.4f + col * 1.6f + row * 0.4f, y, 0.42f)
            }
        }
        fill {
            roundRect(6f, 21f, 42f, 31.2f, 1.2f)
            roundRect(9.5f, 25.5f, 21.5f, 26.6f, 0.55f)
            for (row in 0 until 4) {
                for (col in 0 until 6) circle(28.6f + col * 1.6f, 23f + row * 1.6f, 0.42f)
            }
        }
        fill(alpha = 0.45f) { rect(9f, 31.2f, 39f, 33.2f) }
    }

    private fun consoleSlim() = glyph("console_slim") {
        shadow(21f, 40.6f, 12f)
        fill(alpha = 0.6f) { polygon(19.5f, 9.5f, 28.5f, 5.5f, 28.5f, 34.5f, 19.5f, 38.5f) }
        fill {
            roundRect(12.5f, 9.5f, 19.5f, 38.5f, 1.4f)
            roundRect(15.5f, 12.5f, 16.5f, 30f, 0.5f)
            circle(16f, 34.2f, 0.8f)
        }
        fill(alpha = 0.45f) { polygon(9.5f, 38.5f, 22.5f, 38.5f, 31.5f, 34.5f, 26f, 34.5f, 19.5f, 37.4f, 12.5f, 37.4f) }
        fill {
            roundRect(34f, 12f, 41f, 41f, 3.5f)
            dpad(37.5f, 17.4f, arm = 2.4f, half = 0.75f)
            circle(37.5f, 23.4f, 1.3f)
            circle(37.5f, 33f, 0.7f)
            circle(37.5f, 35.8f, 0.7f)
        }
    }

    private fun consoleCube() = glyph("console_cube") {
        shadow(24f, 42.6f, 14f)
        stroke(width = 2.2f, alpha = 0.85f) {
            moveTo(18.6f, 11f); lineTo(20.6f, 6.6f); lineTo(36f, 6.6f); lineTo(37.6f, 9.6f)
        }
        fill(alpha = 0.6f) { polygon(31f, 19f, 39f, 11f, 39f, 33f, 31f, 41f) }
        fill(alpha = 0.8f) {
            polygon(9f, 19f, 17f, 11f, 39f, 11f, 31f, 19f)
            ellipse(24f, 15f, 9f, 3.1f)
        }
        fill(alpha = 0.92f) { ellipse(24f, 15f, 7.8f, 2.4f) }
        fill {
            roundRect(9f, 19f, 31f, 41f, 1.4f)
            rect(12f, 30f, 19f, 31.2f)
            rect(21f, 30f, 28f, 31.2f)
            roundRect(11.8f, 34.6f, 14.8f, 37.6f, 0.6f)
            roundRect(16.2f, 34.6f, 19.2f, 37.6f, 0.6f)
            roundRect(20.6f, 34.6f, 23.6f, 37.6f, 0.6f)
            roundRect(25f, 34.6f, 28f, 37.6f, 0.6f)
        }
    }

    private fun gamepadSymmetric() = glyph("gamepad_symmetric") {
        fill {
            moveTo(14f, 13f); lineTo(34f, 13f)
            curveTo(39.5f, 13f, 42f, 16f, 43f, 20.5f)
            lineTo(45f, 33f)
            curveTo(45.6f, 37f, 42.8f, 39.5f, 39.8f, 38f)
            curveTo(37.5f, 36.8f, 35.5f, 33f, 33f, 30.5f)
            lineTo(15f, 30.5f)
            curveTo(12.5f, 33f, 10.5f, 36.8f, 8.2f, 38f)
            curveTo(5.2f, 39.5f, 2.4f, 37f, 3f, 33f)
            lineTo(5f, 20.5f)
            curveTo(6f, 16f, 8.5f, 13f, 14f, 13f)
            close()
            roundRect(16.6f, 14.4f, 31.4f, 22f, 1.6f)
            dpad(11.6f, 21.4f, arm = 3f, half = 1f)
            circle(36.4f, 18.8f, 1.2f); circle(39f, 21.4f, 1.2f)
            circle(36.4f, 24f, 1.2f); circle(33.8f, 21.4f, 1.2f)
            circle(18.6f, 26.8f, 2.6f)
            circle(29.4f, 26.8f, 2.6f)
            circle(24f, 26.4f, 0.9f)
        }
        fill(alpha = 0.7f) { roundRect(17.4f, 15.2f, 30.6f, 21.2f, 1.1f) }
        fill { circle(18.6f, 26.8f, 1.6f) }
        fill { circle(29.4f, 26.8f, 1.6f) }
    }

    private fun gamepadTablet() = glyph("gamepad_tablet") {
        fill {
            moveTo(9f, 12.5f); lineTo(39f, 12.5f)
            curveTo(42.6f, 12.5f, 44.5f, 14.5f, 44.5f, 18f)
            lineTo(44.5f, 31f)
            curveTo(44.5f, 34.5f, 42.6f, 36f, 39f, 36f)
            lineTo(9f, 36f)
            curveTo(5.4f, 36f, 3.5f, 34.5f, 3.5f, 31f)
            lineTo(3.5f, 18f)
            curveTo(3.5f, 14.5f, 5.4f, 12.5f, 9f, 12.5f)
            close()
            roundRect(13.5f, 15.2f, 34.5f, 31.2f, 0.8f)
            circle(8.5f, 17.6f, 2f)
            dpad(8.5f, 25.4f, arm = 2.5f, half = 0.85f)
            circle(39.5f, 17.6f, 2f)
            circle(39.5f, 23.3f, 0.85f); circle(41.6f, 25.4f, 0.85f)
            circle(39.5f, 27.5f, 0.85f); circle(37.4f, 25.4f, 0.85f)
            circle(21f, 33.6f, 0.6f); circle(24f, 33.6f, 0.6f); circle(27f, 33.6f, 0.6f)
        }
        fill(alpha = 0.28f) { roundRect(13.5f, 15.2f, 34.5f, 31.2f, 0.8f) }
    }

    private fun gamepadClassic() = glyph("gamepad_classic") {
        fill {
            moveTo(13f, 15f)
            curveTo(18.5f, 15f, 19.5f, 17.6f, 24f, 17.6f)
            curveTo(28.5f, 17.6f, 29.5f, 15f, 35f, 15f)
            arcTo(9f, 9f, 0f, false, true, 35f, 33f)
            curveTo(29.5f, 33f, 28.5f, 30.4f, 24f, 30.4f)
            curveTo(19.5f, 30.4f, 18.5f, 33f, 13f, 33f)
            arcTo(9f, 9f, 0f, false, true, 13f, 15f)
            close()
            dpad(12.8f, 24f, arm = 4.2f, half = 1.35f)
            circle(35.2f, 20.2f, 1.6f); circle(39f, 24f, 1.6f)
            circle(35.2f, 27.8f, 1.6f); circle(31.4f, 24f, 1.6f)
            polygon(19.6f, 26.4f, 21.6f, 24.4f, 22.5f, 25.3f, 20.5f, 27.3f)
            polygon(23.6f, 26.4f, 25.6f, 24.4f, 26.5f, 25.3f, 24.5f, 27.3f)
        }
    }

    private fun handheldTrackpads() = glyph("handheld_trackpads") {
        fill {
            moveTo(9f, 14.5f); lineTo(39f, 14.5f)
            curveTo(43.5f, 14.5f, 45.2f, 17f, 45.4f, 21f)
            lineTo(45.6f, 30f)
            curveTo(45.8f, 34f, 43.5f, 36f, 40.5f, 36f)
            curveTo(37.5f, 36f, 35.5f, 33.2f, 33f, 33.2f)
            lineTo(15f, 33.2f)
            curveTo(12.5f, 33.2f, 10.5f, 36f, 7.5f, 36f)
            curveTo(4.5f, 36f, 2.2f, 34f, 2.4f, 30f)
            lineTo(2.6f, 21f)
            curveTo(2.8f, 17f, 4.5f, 14.5f, 9f, 14.5f)
            close()
            roundRect(14.6f, 16.6f, 33.4f, 29.4f, 0.8f)
            dpad(6.9f, 19.6f, arm = 2.2f, half = 0.7f)
            circle(11.5f, 21f, 1.8f)
            roundRect(8.4f, 25.2f, 13.2f, 30f, 0.9f)
            circle(41.1f, 18.1f, 0.7f); circle(42.6f, 19.6f, 0.7f)
            circle(41.1f, 21.1f, 0.7f); circle(39.6f, 19.6f, 0.7f)
            circle(36.5f, 21f, 1.8f)
            roundRect(34.8f, 25.2f, 39.6f, 30f, 0.9f)
        }
        fill(alpha = 0.28f) { roundRect(14.6f, 16.6f, 33.4f, 29.4f, 0.8f) }
        fill(alpha = 0.6f) {
            roundRect(8.4f, 25.2f, 13.2f, 30f, 0.9f)
            roundRect(34.8f, 25.2f, 39.6f, 30f, 0.9f)
        }
    }

    private fun handheldAngular() = glyph("handheld_angular") {
        fill {
            polygon(
                8.5f, 14.5f, 39.5f, 14.5f, 44.2f, 18.6f, 45.4f, 30.2f, 41.8f, 34.6f, 35.6f, 34.6f,
                33f, 32.4f, 15f, 32.4f, 12.4f, 34.6f, 6.2f, 34.6f, 2.6f, 30.2f, 3.8f, 18.6f,
            )
            roundRect(14f, 16.6f, 34f, 30.2f, 0.6f)
            circle(8.8f, 20.4f, 2.4f)
            dpad(9.6f, 27.6f, arm = 2.4f, half = 0.8f)
            circle(39.2f, 18.6f, 0.8f); circle(41f, 20.4f, 0.8f)
            circle(39.2f, 22.2f, 0.8f); circle(37.4f, 20.4f, 0.8f)
            circle(38.4f, 27.6f, 2.2f)
        }
        fill(alpha = 0.28f) { roundRect(14f, 16.6f, 34f, 30.2f, 0.6f) }
        fill { circle(8.8f, 20.4f, 1.4f) }
        fill { circle(38.4f, 27.6f, 1.3f) }
    }

    private fun handheldDetachable() = glyph("handheld_detachable") {
        fill(alpha = 0.9f) {
            moveTo(11.6f, 12.5f); lineTo(11.6f, 36f); lineTo(5.5f, 36f)
            curveTo(3.8f, 36f, 2.5f, 34.7f, 2.5f, 33f); lineTo(2.5f, 15.5f)
            curveTo(2.5f, 13.8f, 3.8f, 12.5f, 5.5f, 12.5f); close()
            circle(7f, 18f, 2.2f)
            dpad(7f, 26f, arm = 2.4f, half = 0.8f)
        }
        fill { circle(7f, 18f, 1.3f) }
        fill(alpha = 0.9f) {
            moveTo(36.4f, 12.5f); lineTo(36.4f, 36f); lineTo(42.5f, 36f)
            curveTo(44.2f, 36f, 45.5f, 34.7f, 45.5f, 33f); lineTo(45.5f, 15.5f)
            curveTo(45.5f, 13.8f, 44.2f, 12.5f, 42.5f, 12.5f); close()
            circle(41f, 15.8f, 0.75f); circle(42.7f, 17.5f, 0.75f)
            circle(41f, 19.2f, 0.75f); circle(39.3f, 17.5f, 0.75f)
            circle(41f, 24.6f, 2.2f)
            roundRect(38.6f, 29f, 43.4f, 33.6f, 0.8f)
        }
        fill { circle(41f, 24.6f, 1.3f) }
        fill(alpha = 0.6f) { roundRect(38.6f, 29f, 43.4f, 33.6f, 0.8f) }
        fill {
            roundRect(12.3f, 12.5f, 35.7f, 36f, 0.8f)
            roundRect(13.7f, 13.9f, 34.3f, 34.6f, 0.5f)
        }
        fill(alpha = 0.28f) { roundRect(13.7f, 13.9f, 34.3f, 34.6f, 0.5f) }
    }

    private fun handheldGrips() = glyph("handheld_grips") {
        fill {
            moveTo(10f, 14.5f); lineTo(38f, 14.5f)
            curveTo(42.5f, 14.5f, 45f, 17.5f, 45.4f, 22.5f)
            curveTo(45.8f, 28.5f, 45f, 35.5f, 41f, 36.8f)
            curveTo(38f, 37.6f, 35.5f, 34.5f, 33.5f, 32.2f)
            lineTo(14.5f, 32.2f)
            curveTo(12.5f, 34.5f, 10f, 37.6f, 7f, 36.8f)
            curveTo(3f, 35.5f, 2.2f, 28.5f, 2.6f, 22.5f)
            curveTo(3f, 17.5f, 5.5f, 14.5f, 10f, 14.5f)
            close()
            roundRect(14f, 16.6f, 34f, 30.2f, 0.6f)
            circle(8.8f, 20.2f, 2.4f)
            dpad(9.6f, 27.2f, arm = 2.4f, half = 0.8f)
            circle(39.2f, 18.4f, 0.8f); circle(41f, 20.2f, 0.8f)
            circle(39.2f, 22f, 0.8f); circle(37.4f, 20.2f, 0.8f)
            circle(38.4f, 27.2f, 2.2f)
        }
        fill(alpha = 0.28f) { roundRect(14f, 16.6f, 34f, 30.2f, 0.6f) }
        fill { circle(8.8f, 20.2f, 1.4f) }
        fill { circle(38.4f, 27.2f, 1.3f) }
    }

    private fun soundbar() = glyph("soundbar") {
        shadow(24f, 32.8f, 21f)
        fill(alpha = 0.7f) { polygon(6.5f, 18.4f, 41.5f, 18.4f, 45f, 22.4f, 3f, 22.4f) }
        fill {
            roundRect(3f, 22.4f, 45f, 30.8f, 1.8f)
            for (row in 0 until 3) {
                for (col in 0 until 22) circle(6.6f + col * 1.65f, 24.6f + row * 2f, 0.45f)
            }
        }
    }

    private fun tablet() = glyph("tablet") {
        fill {
            roundRect(10f, 6f, 38f, 42f, 3.5f)
            roundRect(12.4f, 8.8f, 35.6f, 39.2f, 1f)
        }
        fill(alpha = 0.3f) { roundRect(12.4f, 8.8f, 35.6f, 39.2f, 1f) }
        fill(alpha = 0.6f, color = Color.Black) { circle(24f, 7.4f, 0.5f) }
    }
}

private class GlyphScope(name: String) {
    private val builder = ImageVector.Builder(
        name = name,
        defaultWidth = 48.dp,
        defaultHeight = 48.dp,
        viewportWidth = 48f,
        viewportHeight = 48f,
    )

    /** Filled shape; overlapping sub-paths cut holes (even-odd), which is how details are drawn. */
    fun fill(alpha: Float = 1f, color: Color = Color.White, block: PathBuilder.() -> Unit) {
        builder.addPath(
            pathData = PathBuilder().apply(block).nodes,
            pathFillType = PathFillType.EvenOdd,
            fill = SolidColor(color),
            fillAlpha = alpha,
        )
    }

    fun stroke(width: Float, alpha: Float = 1f, color: Color = Color.White, block: PathBuilder.() -> Unit) {
        builder.addPath(
            pathData = PathBuilder().apply(block).nodes,
            fill = null,
            stroke = SolidColor(color),
            strokeAlpha = alpha,
            strokeLineWidth = width,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        )
    }

    /** Soft contact shadow under a device. */
    fun shadow(cx: Float, cy: Float, rx: Float) {
        fill(alpha = 0.22f, color = Color.Black) { ellipse(cx, cy, rx, 1.4f) }
    }

    fun build(): ImageVector = builder.build()
}

private fun glyph(name: String, block: GlyphScope.() -> Unit): ImageVector = GlyphScope(name).apply(block).build()

private fun PathBuilder.rect(left: Float, top: Float, right: Float, bottom: Float) {
    moveTo(left, top); lineTo(right, top); lineTo(right, bottom); lineTo(left, bottom); close()
}

private fun PathBuilder.roundRect(left: Float, top: Float, right: Float, bottom: Float, radius: Float) {
    val r = minOf(radius, (right - left) / 2f, (bottom - top) / 2f)
    moveTo(left + r, top)
    lineTo(right - r, top)
    arcTo(r, r, 0f, false, true, right, top + r)
    lineTo(right, bottom - r)
    arcTo(r, r, 0f, false, true, right - r, bottom)
    lineTo(left + r, bottom)
    arcTo(r, r, 0f, false, true, left, bottom - r)
    lineTo(left, top + r)
    arcTo(r, r, 0f, false, true, left + r, top)
    close()
}

private fun PathBuilder.ellipse(cx: Float, cy: Float, rx: Float, ry: Float) {
    moveTo(cx - rx, cy)
    arcTo(rx, ry, 0f, true, true, cx + rx, cy)
    arcTo(rx, ry, 0f, true, true, cx - rx, cy)
    close()
}

private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) = ellipse(cx, cy, r, r)

/** A plus-shaped directional pad centred on ([cx], [cy]); [arm] is the reach from the centre, [half] half the arm width. */
private fun PathBuilder.dpad(cx: Float, cy: Float, arm: Float, half: Float) = polygon(
    cx - half, cy - arm, cx + half, cy - arm, cx + half, cy - half, cx + arm, cy - half,
    cx + arm, cy + half, cx + half, cy + half, cx + half, cy + arm, cx - half, cy + arm,
    cx - half, cy + half, cx - arm, cy + half, cx - arm, cy - half, cx - half, cy - half,
)

private fun PathBuilder.polygon(vararg points: Float) {
    moveTo(points[0], points[1])
    for (i in 2 until points.size step 2) lineTo(points[i], points[i + 1])
    close()
}
