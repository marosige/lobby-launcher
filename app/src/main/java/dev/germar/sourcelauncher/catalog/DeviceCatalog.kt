package dev.germar.sourcelauncher.catalog

import dev.germar.sourcelauncher.model.Glyph
import dev.germar.sourcelauncher.model.Palette

enum class DeviceCategory { CONSOLE, HANDHELD, STREAMING, TV_SERVICE, MEDIA, AUDIO, COMPUTER, OTHER }

/**
 * A recognisable device. [patterns] are regular expressions matched (as whole words) against the
 * normalised label reported by the TV, see [DeviceMatcher.normalize].
 */
data class DeviceProfile(
    val id: String,
    val name: String,
    val category: DeviceCategory,
    val glyph: Glyph,
    val palette: Palette,
    val patterns: List<Regex>,
)

/**
 * A selectable icon in the icon picker: a glyph on a coloured gradient. [suggestedName] is offered as
 * the source name after picking it, or null for icons that do not describe a device.
 */
data class IconPreset(
    val id: String,
    val name: String,
    val category: DeviceCategory,
    val glyph: Glyph,
    val palette: Palette,
    val suggestedName: String? = name,
) {
    /** The name to offer for a source currently called [currentName], or null when there is nothing to offer. */
    fun nameToOffer(currentName: String): String? = offerName(suggestedName, currentName)

    companion object {
        fun offerName(suggestedName: String?, currentName: String): String? {
            val suggestion = suggestedName?.trim().orEmpty()
            val hasSuggestion = suggestion.isNotEmpty()
            val isAlreadyCalledThat = suggestion.equals(currentName.trim(), ignoreCase = true)
            return suggestion.takeIf { hasSuggestion && !isAlreadyCalledThat }
        }
    }
}

/**
 * Known devices, ordered from most to least specific: the first profile whose pattern matches wins.
 * Names are used nominatively to describe the device; all icons are original artwork.
 */
object DeviceCatalog {

    val profiles: List<DeviceProfile> = buildList {
        // PlayStation
        add(device("ps5_pro", "PlayStation 5 Pro", DeviceCategory.CONSOLE, Glyph.CONSOLE_TOWER, Palette.MIDNIGHT, "(play ?station|ps) ?5 ?pro"))
        add(device("ps5", "PlayStation 5", DeviceCategory.CONSOLE, Glyph.CONSOLE_TOWER, Palette.MIDNIGHT, "(play ?station|ps) ?5"))
        add(device("ps4_pro", "PlayStation 4 Pro", DeviceCategory.CONSOLE, Glyph.CONSOLE_SLAB_TRIPLE, Palette.OCEAN, "(play ?station|ps) ?4 ?pro"))
        add(device("ps4", "PlayStation 4", DeviceCategory.CONSOLE, Glyph.CONSOLE_SLAB, Palette.OCEAN, "(play ?station|ps) ?4"))
        add(device("ps3", "PlayStation 3", DeviceCategory.CONSOLE, Glyph.CONSOLE_CURVED, Palette.GRAPHITE, "(play ?station|ps) ?3"))
        add(device("ps2", "PlayStation 2", DeviceCategory.CONSOLE, Glyph.CONSOLE_GROOVED, Palette.INDIGO, "(play ?station|ps) ?2"))
        add(device("playstation", "PlayStation", DeviceCategory.CONSOLE, Glyph.GAMEPAD_SYMMETRIC, Palette.MIDNIGHT, "play ?station( ?(one|1))?", "psx", "sony interactive"))

        // Xbox
        add(device("xbox_series_x", "Xbox Series X", DeviceCategory.CONSOLE, Glyph.CONSOLE_BOX, Palette.EMERALD, "xbox ?series ?x", "xsx"))
        add(device("xbox_series_s", "Xbox Series S", DeviceCategory.CONSOLE, Glyph.CONSOLE_COMPACT, Palette.EMERALD, "xbox ?series ?s", "xss"))
        add(device("xbox_one_x", "Xbox One X", DeviceCategory.CONSOLE, Glyph.CONSOLE_FLAT, Palette.EMERALD, "xbox ?one ?x"))
        add(device("xbox_one_s", "Xbox One S", DeviceCategory.CONSOLE, Glyph.CONSOLE_PERFORATED, Palette.EMERALD, "xbox ?one ?s"))
        add(device("xbox_one", "Xbox One", DeviceCategory.CONSOLE, Glyph.CONSOLE_TWO_TONE, Palette.EMERALD, "xbox ?one", "xb1"))
        add(device("xbox_360", "Xbox 360", DeviceCategory.CONSOLE, Glyph.CONSOLE_CONCAVE, Palette.LIME, "xbox ?360"))
        add(device("xbox", "Xbox", DeviceCategory.CONSOLE, Glyph.GAMEPAD, Palette.EMERALD, "xbox"))

        // Nintendo
        add(device("switch_2", "Nintendo Switch 2", DeviceCategory.CONSOLE, Glyph.HYBRID_CONSOLE, Palette.CRIMSON, "nintendo ?switch ?2", "^switch ?2$"))
        add(device("switch", "Nintendo Switch", DeviceCategory.CONSOLE, Glyph.HYBRID_CONSOLE, Palette.CRIMSON, "nintendo ?switch( ?(oled|lite))?", "^switch( ?oled)?$"))
        add(device("wii_u", "Wii U", DeviceCategory.CONSOLE, Glyph.GAMEPAD_TABLET, Palette.SKY, "wii ?u"))
        add(device("wii", "Wii", DeviceCategory.CONSOLE, Glyph.CONSOLE_SLIM, Palette.SILVER, "wii"))
        add(device("gamecube", "GameCube", DeviceCategory.CONSOLE, Glyph.CONSOLE_CUBE, Palette.INDIGO, "game ?cube", "gcn"))
        add(device("nintendo_classic", "Nintendo Classic Mini", DeviceCategory.CONSOLE, Glyph.CONSOLE_RETRO, Palette.CRIMSON, "s?nes ?(classic|mini)", "super ?nintendo", "snes", "famicom"))
        add(device("nintendo", "Nintendo", DeviceCategory.CONSOLE, Glyph.GAMEPAD_CLASSIC, Palette.CRIMSON, "nintendo"))

        // Valve and handheld PCs
        add(device("steam_machine", "Steam Machine", DeviceCategory.CONSOLE, Glyph.MINI_PC, Palette.STEEL, "steam ?machine"))
        add(device("steam_link", "Steam Link", DeviceCategory.STREAMING, Glyph.STREAM_PUCK, Palette.STEEL, "steam ?link"))
        add(device("steam_deck", "Steam Deck", DeviceCategory.HANDHELD, Glyph.HANDHELD_TRACKPADS, Palette.STEEL, "steam ?deck", "steam ?os", "valve", "steam"))
        add(device("rog_ally", "ROG Ally", DeviceCategory.HANDHELD, Glyph.HANDHELD_ANGULAR, Palette.GRAPHITE, "rog ?ally( ?x)?", "ally ?x"))
        add(device("legion_go", "Legion Go", DeviceCategory.HANDHELD, Glyph.HANDHELD_DETACHABLE, Palette.SILVER, "legion ?go( ?s)?"))
        add(device("msi_claw", "MSI Claw", DeviceCategory.HANDHELD, Glyph.HANDHELD_GRIPS, Palette.CRIMSON, "msi ?claw", "claw"))
        add(device("handheld_pc", "Handheld PC", DeviceCategory.HANDHELD, Glyph.HANDHELD_PC, Palette.INDIGO, "aya ?neo", "gpd ?win", "onexplayer", "handheld"))

        // Retro
        add(device("retro", "Retro Console", DeviceCategory.CONSOLE, Glyph.CONSOLE_RETRO, Palette.GOLD, "sega", "mega ?drive", "genesis", "dreamcast", "atari", "neo ?geo", "evercade", "analogue", "mister", "retro ?pie", "recalbox", "batocera", "retro"))

        // Streaming boxes and sticks
        add(device("apple_tv", "Apple TV", DeviceCategory.STREAMING, Glyph.STREAM_BOX, Palette.GRAPHITE, "apple ?tv"))
        add(device("fire_tv_cube", "Fire TV Cube", DeviceCategory.STREAMING, Glyph.STREAM_BOX, Palette.TANGERINE, "fire ?tv ?cube"))
        add(device("fire_tv", "Fire TV", DeviceCategory.STREAMING, Glyph.STREAM_STICK, Palette.TANGERINE, "fire ?tv( ?stick)?", "fire ?stick", "amazon", "aft(?!er)[a-z0-9]{1,6}"))
        add(device("google_tv_streamer", "Google TV Streamer", DeviceCategory.STREAMING, Glyph.STREAM_PUCK, Palette.SKY, "google ?tv ?streamer"))
        add(device("chromecast", "Chromecast", DeviceCategory.STREAMING, Glyph.STREAM_PUCK, Palette.SKY, "chromecast", "google ?tv", "google"))
        add(device("roku", "Roku", DeviceCategory.STREAMING, Glyph.STREAM_STICK, Palette.VIOLET, "roku"))
        add(device("shield", "NVIDIA SHIELD", DeviceCategory.STREAMING, Glyph.STREAM_PUCK, Palette.LIME, "nvidia ?shield", "shield( ?(tv|pro))?", "nvidia"))
        add(device("xiaomi_box", "Xiaomi TV Box", DeviceCategory.STREAMING, Glyph.STREAM_BOX, Palette.SUNSET, "mi ?box( ?s)?", "mi ?tv ?stick", "xiaomi"))
        add(device("onn", "onn. 4K", DeviceCategory.STREAMING, Glyph.STREAM_BOX, Palette.OCEAN, "onn"))
        add(device("media_player", "Media Player", DeviceCategory.STREAMING, Glyph.STREAM_BOX, Palette.TEAL, "formuler", "zidoo", "dune ?hd", "dune", "homatics", "ugoos", "tv ?box", "android ?(tv|box)", "ott"))

        // TV operators and set-top boxes
        add(device("magenta_tv", "MagentaTV", DeviceCategory.TV_SERVICE, Glyph.SET_TOP_BOX, Palette.MAGENTA, "magenta ?(tv|one)?", "telekom", "t ?home", "entertain ?tv"))
        add(device("vodafone_tv", "Vodafone TV", DeviceCategory.TV_SERVICE, Glyph.SET_TOP_BOX, Palette.CRIMSON, "vodafone", "giga ?tv", "horizon", "upc"))
        add(device("one_tv", "One TV", DeviceCategory.TV_SERVICE, Glyph.SET_TOP_BOX, Palette.INDIGO, "one ?tv"))
        add(device("digi_tv", "DIGI TV", DeviceCategory.TV_SERVICE, Glyph.SET_TOP_BOX, Palette.OCEAN, "digi( ?tv)?"))
        add(device("sky", "Sky", DeviceCategory.TV_SERVICE, Glyph.SET_TOP_BOX, Palette.INDIGO, "sky( ?(q|stream|glass|box|hd|plus))?"))
        add(device("virgin_tv", "Virgin TV", DeviceCategory.TV_SERVICE, Glyph.SET_TOP_BOX, Palette.CRIMSON, "virgin( ?(tv|media))?"))
        add(device("tivo", "TiVo", DeviceCategory.TV_SERVICE, Glyph.SET_TOP_BOX, Palette.GRAPHITE, "tivo"))
        add(device("youview", "YouView", DeviceCategory.TV_SERVICE, Glyph.SET_TOP_BOX, Palette.VIOLET, "youview", "bt ?tv", "ee ?tv"))
        add(device("xfinity", "Xfinity", DeviceCategory.TV_SERVICE, Glyph.SET_TOP_BOX, Palette.VIOLET, "xfinity", "comcast"))
        add(device("directv", "DIRECTV", DeviceCategory.TV_SERVICE, Glyph.SET_TOP_BOX, Palette.OCEAN, "direc ?tv"))
        add(device("canal_plus", "Canal+", DeviceCategory.TV_SERVICE, Glyph.SET_TOP_BOX, Palette.GRAPHITE, "canal ?(plus)?"))
        add(device("orange_tv", "Orange TV", DeviceCategory.TV_SERVICE, Glyph.SET_TOP_BOX, Palette.TANGERINE, "orange ?tv", "livebox"))
        add(device("operator_box", "TV Box", DeviceCategory.TV_SERVICE, Glyph.SET_TOP_BOX, Palette.TEAL, "freebox", "bbox", "sfr", "movistar", "telenet", "proximus", "swisscom", "blue ?tv", "a1 ?(tv|xplore)", "foxtel", "spectrum", "dish ?(network|hopper)?", "hopper", "humax", "arris", "commscope", "kaon", "technicolor", "freesat"))
        add(device("satellite", "Satellite Receiver", DeviceCategory.TV_SERVICE, Glyph.SATELLITE, Palette.STEEL, "satellite", "sat ?receiver", "dvb ?s2?", "dreambox", "vu ?(plus|uno|duo|solo|zero|ultimo)", "zgemma", "enigma ?2?", "gigablue", "octagon"))
        add(device("antenna", "Live TV", DeviceCategory.TV_SERVICE, Glyph.ANTENNA, Palette.SILVER, "antenna", "aerial", "live ?tv", "dvb ?[tc]2?", "terrestrial", "broadcast", "tv ?tuner", "freeview", "^tv$"))
        add(device("set_top_box", "Set-top Box", DeviceCategory.TV_SERVICE, Glyph.SET_TOP_BOX, Palette.TEAL, "set ?top( ?box)?", "stb", "cable ?box", "decoder", "iptv", "^tuner( ?\\d)?$"))

        // Media players and recorders
        add(device("bluray", "Blu-ray Player", DeviceCategory.MEDIA, Glyph.DISC, Palette.INDIGO, "blu ?ray", "uhd ?player", "bd ?player", "bdp", "bd", "disc ?player"))
        add(device("dvd", "DVD Player", DeviceCategory.MEDIA, Glyph.DISC, Palette.GRAPHITE, "dvd"))
        add(device("recorder", "Recorder", DeviceCategory.MEDIA, Glyph.SET_TOP_BOX, Palette.GRAPHITE, "recorder( ?\\d)?", "pvr", "dvr", "vcr", "vhs"))
        add(device("raspberry_pi", "Raspberry Pi", DeviceCategory.COMPUTER, Glyph.BOARD, Palette.CRIMSON, "raspberry( ?pi)?", "rpi"))
        add(device("media_center", "Media Center", DeviceCategory.MEDIA, Glyph.PLAY, Palette.TEAL, "kodi", "libre ?elec", "core ?elec", "osmc", "plex", "media ?(center|centre)"))

        // Audio
        add(device("soundbar", "Soundbar", DeviceCategory.AUDIO, Glyph.SOUNDBAR, Palette.GRAPHITE, "sound ?bar", "sonos", "bose"))
        add(device("av_receiver", "AV Receiver", DeviceCategory.AUDIO, Glyph.RECEIVER, Palette.GOLD, "av ?receiver", "avr", "receiver", "amplifier", "denon", "marantz", "yamaha", "onkyo", "pioneer", "harman", "arcam", "home ?theat(er|re)", "audio ?system"))

        // Computers and other devices
        add(device("macbook", "MacBook", DeviceCategory.COMPUTER, Glyph.LAPTOP, Palette.SILVER, "macbook( ?(air|pro))?"))
        add(device("mac", "Mac", DeviceCategory.COMPUTER, Glyph.MINI_PC, Palette.SILVER, "mac ?(mini|studio|pro)", "imac"))
        add(device("laptop", "Laptop", DeviceCategory.COMPUTER, Glyph.LAPTOP, Palette.STEEL, "laptop", "notebook", "thinkpad", "chromebook", "surface"))
        add(device("pc", "PC", DeviceCategory.COMPUTER, Glyph.DESKTOP, Palette.STEEL, "gaming ?pc", "pc", "computer", "desktop", "windows", "htpc", "nuc", "linux", "workstation"))
        add(device("camera", "Camera", DeviceCategory.OTHER, Glyph.CAMERA, Palette.GRAPHITE, "camera", "camcorder", "gopro", "cam"))
        add(device("tablet", "Tablet", DeviceCategory.OTHER, Glyph.TABLET, Palette.SKY, "ipad", "tablet", "galaxy ?tab", "tab ?s\\d+"))
        add(device("phone", "Phone", DeviceCategory.OTHER, Glyph.PHONE, Palette.OCEAN, "iphone", "phone", "galaxy", "pixel", "dex"))
    }

    private val profilesById = profiles.associateBy { it.id }

    fun byId(id: String?): DeviceProfile? = id?.let { profilesById[it] }

    /** Generic icons that are not tied to a particular device. */
    val genericPresets: List<IconPreset> = listOf(
        IconPreset("generic_hdmi", "HDMI", DeviceCategory.OTHER, Glyph.HDMI, Palette.SILVER),
        IconPreset("generic_gamepad", "Game Console", DeviceCategory.CONSOLE, Glyph.GAMEPAD, Palette.VIOLET),
        IconPreset("generic_stream", "Streaming Box", DeviceCategory.STREAMING, Glyph.STREAM_BOX, Palette.TEAL),
        IconPreset("generic_stick", "Streaming Stick", DeviceCategory.STREAMING, Glyph.STREAM_STICK, Palette.OCEAN),
        IconPreset("generic_av", "AV / Composite", DeviceCategory.OTHER, Glyph.AV, Palette.GOLD, suggestedName = "AV"),
        IconPreset("generic_play", "Media", DeviceCategory.MEDIA, Glyph.PLAY, Palette.SUNSET),
        IconPreset("generic_star", "Favourite", DeviceCategory.OTHER, Glyph.STAR, Palette.GOLD, suggestedName = null),
        IconPreset("generic_monogram", "Initials", DeviceCategory.OTHER, Glyph.MONOGRAM, Palette.INDIGO, suggestedName = null),
    )

    /**
     * Every catalogue device and generic icon, grouped by category in catalogue order. Devices that
     * share an icon are all listed so each can be picked by name.
     */
    val presets: List<IconPreset> by lazy {
        val fromProfiles = profiles.map { IconPreset(it.id, it.name, it.category, it.glyph, it.palette) }
        (fromProfiles + genericPresets).sortedBy { it.category.ordinal }
    }

    private fun device(
        id: String,
        name: String,
        category: DeviceCategory,
        glyph: Glyph,
        palette: Palette,
        vararg patterns: String,
    ) = DeviceProfile(
        id = id,
        name = name,
        category = category,
        glyph = glyph,
        palette = palette,
        patterns = patterns.map { Regex("(?<![a-z0-9])(?:$it)(?![a-z0-9])") },
    )
}
