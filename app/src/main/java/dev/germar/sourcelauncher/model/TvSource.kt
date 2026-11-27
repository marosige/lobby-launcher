package dev.germar.sourcelauncher.model

enum class SourceKind { HDMI, COMPONENT, COMPOSITE, SVIDEO, SCART, VGA, DVI, DISPLAY_PORT, TUNER, OTHER }

enum class ConnectionState { CONNECTED, STANDBY, DISCONNECTED, UNKNOWN }

/**
 * One physical input of the TV as reported by the system, independent of user customisation.
 *
 * [id] is the stable hardware input id (the HDMI port), which is what settings are keyed on.
 * [deviceLabels] holds the names of devices detected on that port (HDMI-CEC OSD names such as
 * "PlayStation 5"), which change when another device is plugged in.
 */
data class TvSource(
    val id: String,
    val kind: SourceKind,
    val portLabel: String,
    val customLabel: String? = null,
    val deviceLabels: List<String> = emptyList(),
    val connection: ConnectionState = ConnectionState.UNKNOWN,
    val isPassthrough: Boolean = true,
    val hiddenBySystem: Boolean = false,
)
