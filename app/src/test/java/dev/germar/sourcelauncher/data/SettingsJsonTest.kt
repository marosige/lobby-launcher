package dev.germar.sourcelauncher.data

import dev.germar.sourcelauncher.model.ClockFormat
import dev.germar.sourcelauncher.model.LauncherSettings
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsJsonTest {

    private fun decode(json: String) = settingsJson.decodeFromString(LauncherSettings.serializer(), json)

    @Test
    fun `clock format survives a round trip`() {
        val settings = LauncherSettings(clockFormat = ClockFormat.H12)
        val json = settingsJson.encodeToString(LauncherSettings.serializer(), settings)
        assertEquals(ClockFormat.H12, decode(json).clockFormat)
    }

    @Test
    fun `settings saved before the clock option default to the system format`() {
        assertEquals(ClockFormat.SYSTEM, decode("""{"autoSelectEnabled":true}""").clockFormat)
    }

    @Test
    fun `an unknown clock format falls back to the system format`() {
        val settings = decode("""{"clockFormat":"H36","autoSelectSeconds":5}""")
        assertEquals(ClockFormat.SYSTEM, settings.clockFormat)
        assertEquals(5, settings.autoSelectSeconds)
    }
}
