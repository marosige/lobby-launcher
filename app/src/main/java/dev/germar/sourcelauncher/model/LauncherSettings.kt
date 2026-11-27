package dev.germar.sourcelauncher.model

import kotlinx.serialization.Serializable

/** User customisation of one source. `null` fields fall back to what the TV reports. */
@Serializable
data class SourceOverride(
    val name: String? = null,
    val glyph: String? = null,
    val palette: String? = null,
    val hidden: Boolean? = null,
) {
    val isEmpty: Boolean get() = this == SourceOverride()
}

/** How the clock on the home screen shows the time. */
@Serializable
enum class ClockFormat { SYSTEM, H12, H24 }

@Serializable
data class LauncherSettings(
    val overrides: Map<String, SourceOverride> = emptyMap(),
    val order: List<String> = emptyList(),
    val defaultSourceId: String? = null,
    val lastUsedSourceId: String? = null,
    val autoSelectEnabled: Boolean = false,
    val autoSelectSeconds: Int = DEFAULT_AUTO_SELECT_SECONDS,
    val autoSelectOnlyWhenConnected: Boolean = true,
    val showDisconnected: Boolean = true,
    val showTunerInputs: Boolean = false,
    val demoMode: Boolean = false,
    val interceptHomeButton: Boolean = true,
    val showOnWake: Boolean = false,
    val replaceStockHome: Boolean = false,
    val clockFormat: ClockFormat = ClockFormat.SYSTEM,
) {
    fun override(id: String): SourceOverride = overrides[id] ?: SourceOverride()

    fun withOverride(id: String, transform: (SourceOverride) -> SourceOverride): LauncherSettings {
        val updated = transform(override(id))
        val newOverrides = if (updated.isEmpty) overrides - id else overrides + (id to updated)
        return copy(overrides = newOverrides)
    }

    companion object {
        const val DEFAULT_AUTO_SELECT_SECONDS = 10
        val AUTO_SELECT_CHOICES = listOf(3, 5, 8, 10, 15, 20, 30, 45, 60)
    }
}
