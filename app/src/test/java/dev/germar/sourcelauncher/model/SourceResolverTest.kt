package dev.germar.sourcelauncher.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SourceResolverTest {

    private val appleTv = TvSource(
        id = "hw1",
        kind = SourceKind.HDMI,
        portLabel = "HDMI 1",
        deviceLabels = listOf("Apple TV"),
        connection = ConnectionState.CONNECTED,
    )
    private val ps5 = TvSource(
        id = "hw2",
        kind = SourceKind.HDMI,
        portLabel = "HDMI 2",
        deviceLabels = listOf("PS5"),
        connection = ConnectionState.STANDBY,
    )
    private val empty = TvSource(
        id = "hw3",
        kind = SourceKind.HDMI,
        portLabel = "HDMI 3",
        connection = ConnectionState.DISCONNECTED,
    )
    private val sources = listOf(appleTv, ps5, empty)

    @Test
    fun `detected devices get catalog name icon and port subtitle`() {
        val resolved = SourceResolver.resolve(sources, LauncherSettings())

        assertEquals(listOf("Apple TV", "PlayStation 5", "HDMI 3"), resolved.map { it.name })
        assertEquals(Glyph.CONSOLE_TOWER, resolved[1].appearance.glyph)
        assertEquals("HDMI 2", resolved[1].portLabel)
        assertEquals(Glyph.HDMI, resolved[2].appearance.glyph)
        assertNull(resolved[2].portLabel)
    }

    @Test
    fun `tv custom label wins over detected device name`() {
        val renamedOnTv = ps5.copy(customLabel = "Gergő PS5")

        val resolved = SourceResolver.resolveOne(renamedOnTv, LauncherSettings())

        assertEquals("Gergő PS5", resolved.name)
        assertEquals(Glyph.CONSOLE_TOWER, resolved.appearance.glyph)
    }

    @Test
    fun `user overrides replace name and icon but keep detection available`() {
        val settings = LauncherSettings().withOverride("hw2") {
            it.copy(name = "Játék", glyph = Glyph.GAMEPAD.name, palette = Palette.CRIMSON.name)
        }

        val resolved = SourceResolver.resolveOne(ps5, settings)

        assertEquals("Játék", resolved.name)
        assertEquals("PlayStation 5", resolved.detectedName)
        assertEquals(Appearance(Glyph.GAMEPAD, Palette.CRIMSON), resolved.appearance)
        assertEquals(Appearance(Glyph.CONSOLE_TOWER, Palette.MIDNIGHT), resolved.detectedAppearance)
        assertTrue(resolved.hasCustomName)
        assertTrue(resolved.hasCustomIcon)
    }

    @Test
    fun `unknown persisted icon names fall back to detection`() {
        val settings = LauncherSettings().withOverride("hw1") { it.copy(glyph = "REMOVED_GLYPH") }

        val resolved = SourceResolver.resolveOne(appleTv, settings)

        assertEquals(Glyph.STREAM_BOX, resolved.appearance.glyph)
        assertFalse(resolved.hasCustomIcon)
    }

    @Test
    fun `clearing every override field removes the entry`() {
        val settings = LauncherSettings()
            .withOverride("hw1") { it.copy(name = "TV box") }
            .withOverride("hw1") { it.copy(name = null) }

        assertTrue(settings.overrides.isEmpty())
    }

    @Test
    fun `saved order is applied and new sources are appended`() {
        val settings = LauncherSettings(order = listOf("hw2", "hw1"))

        val resolved = SourceResolver.resolve(sources, settings)

        assertEquals(listOf("hw2", "hw1", "hw3"), resolved.map { it.id })
    }

    @Test
    fun `move clamps to bounds`() {
        val order = listOf("a", "b", "c")

        assertEquals(listOf("b", "a", "c"), SourceOrder.move(order, "a", 1))
        assertEquals(listOf("c", "a", "b"), SourceOrder.move(order, "c", -5))
        assertEquals(order, SourceOrder.move(order, "c", 1))
        assertEquals(order, SourceOrder.move(order, "missing", 1))
    }

    @Test
    fun `home hides hidden and optionally disconnected sources`() {
        val settings = LauncherSettings(showDisconnected = false).withOverride("hw1") { it.copy(hidden = true) }
        val all = SourceResolver.resolve(sources, settings)

        val home = SourceResolver.visibleOnHome(all, settings)

        assertEquals(listOf("hw2"), home.map { it.id })
    }

    @Test
    fun `inputs hidden in tv settings start hidden but can be shown`() {
        val hiddenOnTv = empty.copy(hiddenBySystem = true)

        assertTrue(SourceResolver.resolveOne(hiddenOnTv, LauncherSettings()).hidden)
        val shown = LauncherSettings().withOverride("hw3") { it.copy(hidden = false) }
        assertFalse(SourceResolver.resolveOne(hiddenOnTv, shown).hidden)
    }

    @Test
    fun `built-in tuner is shown as live tv`() {
        val tuner = TvSource(id = "tuner", kind = SourceKind.TUNER, portLabel = "Tuner")

        val resolved = SourceResolver.resolveOne(tuner, LauncherSettings())

        assertEquals("Live TV", resolved.name)
        assertEquals(Glyph.ANTENNA, resolved.appearance.glyph)
    }

    @Test
    fun `auto select targets the default source only when enabled and available`() {
        val base = LauncherSettings(autoSelectEnabled = true, defaultSourceId = "hw2")
        val home = SourceResolver.resolve(sources, base)

        assertEquals("hw2", AutoSelectPolicy.target(home, base)?.id)
        assertNull(AutoSelectPolicy.target(home, base.copy(autoSelectEnabled = false)))
        assertNull(AutoSelectPolicy.target(home, base.copy(defaultSourceId = "hw3")))
        assertEquals("hw3", AutoSelectPolicy.target(home, base.copy(defaultSourceId = "hw3", autoSelectOnlyWhenConnected = false))?.id)
        assertNull(AutoSelectPolicy.target(home, base.copy(defaultSourceId = "gone")))
    }

    @Test
    fun `initial focus prefers countdown target then last used then first connected`() {
        val home = SourceResolver.resolve(sources, LauncherSettings())

        assertEquals("hw2", AutoSelectPolicy.initialFocus(home, LauncherSettings(autoSelectEnabled = true, defaultSourceId = "hw2", lastUsedSourceId = "hw1"))?.id)
        assertEquals("hw1", AutoSelectPolicy.initialFocus(home, LauncherSettings(lastUsedSourceId = "hw1"))?.id)
        val disconnectedFirst = SourceResolver.resolve(sources, LauncherSettings(order = listOf("hw3")))
        assertEquals("hw1", AutoSelectPolicy.initialFocus(disconnectedFirst, LauncherSettings())?.id)
    }
}
