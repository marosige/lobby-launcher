package dev.germar.sourcelauncher.model

import dev.germar.sourcelauncher.catalog.DeviceCatalog
import dev.germar.sourcelauncher.catalog.DeviceMatcher
import dev.germar.sourcelauncher.catalog.DeviceProfile

/** A source as shown to the user: the TV's data merged with detection and user customisation. */
data class ResolvedSource(
    val source: TvSource,
    val name: String,
    val detectedName: String,
    val portLabel: String?,
    val appearance: Appearance,
    val detectedAppearance: Appearance,
    val profile: DeviceProfile?,
    val hidden: Boolean,
    val isDefault: Boolean,
    val hasCustomName: Boolean,
    val hasCustomIcon: Boolean,
) {
    val id: String get() = source.id
    val connection: ConnectionState get() = source.connection
    val isDisconnected: Boolean get() = source.connection == ConnectionState.DISCONNECTED
}

object SourceResolver {

    /** All sources, including hidden ones, in the user's order. */
    fun resolve(sources: List<TvSource>, settings: LauncherSettings): List<ResolvedSource> =
        SourceOrder.apply(sources, settings.order) { it.id }.map { resolveOne(it, settings) }

    fun resolveOne(source: TvSource, settings: LauncherSettings): ResolvedSource {
        val override = settings.override(source.id)
        val profile = detectProfile(source)
        val detectedName = detectedName(source, profile)
        val detectedAppearance = profile?.let { Appearance(it.glyph, it.palette) } ?: fallbackAppearance(source.kind)

        val customName = override.name?.trim()?.takeIf { it.isNotEmpty() }
        val customGlyph = enumOrNull(Glyph.entries.toTypedArray(), override.glyph)
        val customPalette = enumOrNull(Palette.entries.toTypedArray(), override.palette)
        val appearance = Appearance(
            glyph = customGlyph ?: detectedAppearance.glyph,
            palette = customPalette ?: detectedAppearance.palette,
        )
        val name = customName ?: detectedName
        val portLabel = source.portLabel.takeIf { it.isNotBlank() && it != name }

        return ResolvedSource(
            source = source,
            name = name,
            detectedName = detectedName,
            portLabel = portLabel,
            appearance = appearance,
            detectedAppearance = detectedAppearance,
            profile = profile,
            hidden = override.hidden ?: source.hiddenBySystem,
            isDefault = settings.defaultSourceId == source.id,
            hasCustomName = customName != null,
            hasCustomIcon = customGlyph != null || customPalette != null,
        )
    }

    /** Sources that belong on the home screen. */
    fun visibleOnHome(all: List<ResolvedSource>, settings: LauncherSettings): List<ResolvedSource> =
        all.filter { source ->
            val isHidden = source.hidden
            val isHiddenBecauseDisconnected = !settings.showDisconnected && source.isDisconnected
            !isHidden && !isHiddenBecauseDisconnected
        }

    fun detectProfile(source: TvSource): DeviceProfile? {
        val labels = listOf(source.customLabel) + source.deviceLabels + source.portLabel
        val profile = DeviceMatcher.matchFirst(labels)
        val isBuiltInTuner = source.kind == SourceKind.TUNER
        val looksLikeGenericTuner = profile == null || profile.id == "set_top_box"
        return if (isBuiltInTuner && looksLikeGenericTuner) DeviceCatalog.byId("antenna") else profile
    }

    /**
     * The user's own naming in the TV's input settings wins, then the recognised device name, then the
     * raw HDMI-CEC device name, then the port ("HDMI 2").
     */
    fun detectedName(source: TvSource, profile: DeviceProfile?): String {
        val tvCustomLabel = source.customLabel?.trim()?.takeUnless { DeviceMatcher.isGenericPortLabel(it) }
        val deviceLabel = source.deviceLabels.map { it.trim() }.firstOrNull { !DeviceMatcher.isGenericPortLabel(it) }
        return tvCustomLabel ?: profile?.name ?: deviceLabel ?: source.portLabel
    }

    fun fallbackAppearance(kind: SourceKind): Appearance = when (kind) {
        SourceKind.HDMI -> Appearance(Glyph.HDMI, Palette.SILVER)
        SourceKind.COMPONENT, SourceKind.COMPOSITE, SourceKind.SVIDEO, SourceKind.SCART -> Appearance(Glyph.AV, Palette.GOLD)
        SourceKind.VGA, SourceKind.DVI, SourceKind.DISPLAY_PORT -> Appearance(Glyph.DESKTOP, Palette.STEEL)
        SourceKind.TUNER -> Appearance(Glyph.ANTENNA, Palette.SILVER)
        SourceKind.OTHER -> Appearance(Glyph.PLAY, Palette.TEAL)
    }
}

object SourceOrder {

    /** Items in the saved order first; anything not in it keeps the TV's order at the end. */
    fun <T> apply(items: List<T>, savedOrder: List<String>, idOf: (T) -> String): List<T> {
        val rank = savedOrder.withIndex().associate { it.value to it.index }
        val (known, unknown) = items.partition { idOf(it) in rank }
        return known.sortedBy { rank.getValue(idOf(it)) } + unknown
    }

    /** Moves [id] by [delta] positions, clamped to the list bounds. */
    fun move(order: List<String>, id: String, delta: Int): List<String> {
        val from = order.indexOf(id)
        if (from < 0) return order
        val to = (from + delta).coerceIn(0, order.lastIndex)
        if (to == from) return order
        return order.toMutableList().apply { add(to, removeAt(from)) }
    }
}

object AutoSelectPolicy {

    /** The source the countdown should switch to, or null when auto-select should not run. */
    fun target(home: List<ResolvedSource>, settings: LauncherSettings): ResolvedSource? {
        if (!settings.autoSelectEnabled) return null
        val candidate = home.firstOrNull { it.id == settings.defaultSourceId } ?: return null
        val mustBeConnected = settings.autoSelectOnlyWhenConnected
        val isUnavailable = candidate.isDisconnected
        if (mustBeConnected && isUnavailable) return null
        return candidate
    }

    /** Which tile gets focus first: the countdown target, then the last used source, then the first connected. */
    fun initialFocus(home: List<ResolvedSource>, settings: LauncherSettings): ResolvedSource? {
        val countdownTarget = target(home, settings)
        val lastUsed = home.firstOrNull { it.id == settings.lastUsedSourceId }
        val firstConnected = home.firstOrNull { !it.isDisconnected }
        return countdownTarget ?: lastUsed ?: firstConnected ?: home.firstOrNull()
    }
}
