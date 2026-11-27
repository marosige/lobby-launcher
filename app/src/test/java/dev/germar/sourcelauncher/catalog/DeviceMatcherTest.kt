package dev.germar.sourcelauncher.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeviceMatcherTest {

    private fun idOf(label: String): String? = DeviceMatcher.match(label)?.id

    @Test
    fun `normalize strips trademarks accents and punctuation`() {
        assertEquals("playstation5", DeviceMatcher.normalize("PlayStation®5"))
        assertEquals("playstation 5", DeviceMatcher.normalize("PlayStation(R) 5"))
        assertEquals("jatekkonzol", DeviceMatcher.normalize("Játékkonzol"))
        assertEquals("blu ray player", DeviceMatcher.normalize("  Blu-ray  Player "))
    }

    @Test
    fun `recognises PlayStation generations as reported over HDMI-CEC`() {
        assertEquals("ps5", idOf("PlayStation 5"))
        assertEquals("ps5", idOf("PlayStation®5"))
        assertEquals("ps5", idOf("PS5"))
        assertEquals("ps5", idOf("PlayStation(R)5 Digital Edition"))
        assertEquals("ps5_pro", idOf("PS5 Pro"))
        assertEquals("ps4", idOf("PS4"))
        assertEquals("ps4_pro", idOf("PlayStation 4 Pro"))
        assertEquals("ps3", idOf("PLAYSTATION 3"))
        assertEquals("playstation", idOf("PlayStation"))
    }

    @Test
    fun `recognises Xbox models with the most specific one winning`() {
        assertEquals("xbox_series_x", idOf("Xbox Series X"))
        assertEquals("xbox_series_x", idOf("XboxSeriesX"))
        assertEquals("xbox_series_s", idOf("XBOX SERIES S"))
        assertEquals("xbox_one_x", idOf("Xbox One X"))
        assertEquals("xbox_one", idOf("Xbox One"))
        assertEquals("xbox_360", idOf("Xbox 360"))
        assertEquals("xbox", idOf("Xbox"))
    }

    @Test
    fun `recognises Nintendo and Valve devices`() {
        assertEquals("switch", idOf("NintendoSwitch"))
        assertEquals("switch", idOf("Nintendo Switch OLED"))
        assertEquals("switch", idOf("Switch"))
        assertEquals("switch_2", idOf("Nintendo Switch 2"))
        assertEquals("wii_u", idOf("Wii U"))
        assertEquals("steam_deck", idOf("Steam Deck"))
        assertEquals("steam_deck", idOf("SteamOS"))
        assertEquals("steam_machine", idOf("Steam Machine"))
        assertEquals("rog_ally", idOf("ROG Ally X"))
    }

    @Test
    fun `recognises streaming boxes and set-top boxes`() {
        assertEquals("apple_tv", idOf("Apple TV"))
        assertEquals("apple_tv", idOf("AppleTV 4K"))
        assertEquals("fire_tv", idOf("Fire TV Stick"))
        assertEquals("fire_tv", idOf("Amazon Fire TV"))
        assertEquals("fire_tv", idOf("AFTKA"))
        assertEquals("fire_tv_cube", idOf("Fire TV Cube"))
        assertEquals("chromecast", idOf("Chromecast"))
        assertEquals("google_tv_streamer", idOf("Google TV Streamer"))
        assertEquals("shield", idOf("SHIELD"))
        assertEquals("roku", idOf("Roku Ultra"))
        assertEquals("magenta_tv", idOf("MagentaTV One"))
        assertEquals("magenta_tv", idOf("Telekom"))
        assertEquals("vodafone_tv", idOf("Vodafone TV"))
        assertEquals("sky", idOf("Sky Q"))
        assertEquals("set_top_box", idOf("Tuner 1"))
    }

    @Test
    fun `recognises generic HDMI-CEC device types and other devices`() {
        assertEquals("av_receiver", idOf("Audio System"))
        assertEquals("av_receiver", idOf("Denon AVR-X2800H"))
        assertEquals("recorder", idOf("Recorder 1"))
        assertEquals("bluray", idOf("Sony BDP-S6700"))
        assertEquals("pc", idOf("Gaming PC"))
        assertEquals("macbook", idOf("MacBook Pro"))
        assertEquals("soundbar", idOf("Sonos Arc"))
    }

    @Test
    fun `tells phones and tablets apart`() {
        assertEquals("tablet", idOf("iPad Pro"))
        assertEquals("tablet", idOf("Galaxy Tab S9"))
        assertEquals("tablet", idOf("Tab S8"))
        assertEquals("phone", idOf("iPhone"))
        assertEquals("phone", idOf("Pixel 8"))
        assertEquals("phone", idOf("Samsung DeX"))
    }

    @Test
    fun `each console generation has its own icon`() {
        val playStations = listOf("ps2", "ps3", "ps4", "ps4_pro", "ps5", "playstation")
        val xboxes = listOf("xbox_360", "xbox_one", "xbox_one_s", "xbox_one_x", "xbox_series_s", "xbox_series_x")
        val nintendos = listOf("gamecube", "wii", "wii_u", "switch", "nintendo", "nintendo_classic")
        val handhelds = listOf("steam_deck", "rog_ally", "legion_go", "msi_claw", "handheld_pc")
        for (family in listOf(playStations, xboxes, nintendos, handhelds)) {
            val glyphs = family.map { id -> requireNotNull(DeviceCatalog.byId(id)) { id }.glyph }
            assertEquals("$family", family.size, glyphs.toSet().size)
        }
    }

    @Test
    fun `does not invent devices from generic or ambiguous labels`() {
        assertNull(idOf("HDMI 2"))
        assertNull(idOf("HDMI 1 (eARC)"))
        assertNull(idOf("HDMI Switch"))
        assertNull(idOf("Playback 1"))
        assertNull(idOf("Digital"))
        assertNull(idOf("Aftermath"))
        assertNull(idOf("Skyworth"))
        assertNull(idOf(""))
    }

    @Test
    fun `matchFirst prefers earlier labels`() {
        assertEquals("apple_tv", DeviceMatcher.matchFirst(listOf(null, "Apple TV", "PS5"))?.id)
        assertEquals("ps5", DeviceMatcher.matchFirst(listOf("HDMI 2", "PS5"))?.id)
    }

    @Test
    fun `generic port labels are identified`() {
        assertTrue(DeviceMatcher.isGenericPortLabel("HDMI 3"))
        assertTrue(DeviceMatcher.isGenericPortLabel("HDMI 1 eARC"))
        assertTrue(DeviceMatcher.isGenericPortLabel("Playback 2"))
        assertTrue(DeviceMatcher.isGenericPortLabel("AV"))
        assertFalse(DeviceMatcher.isGenericPortLabel("Living room PC"))
    }

    @Test
    fun `catalog ids are unique and every profile has patterns`() {
        val ids = DeviceCatalog.profiles.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        assertTrue(DeviceCatalog.profiles.all { it.patterns.isNotEmpty() })
    }

    @Test
    fun `icon picker lists every catalogue device by name`() {
        val presetIds = DeviceCatalog.presets.map { it.id }.toSet()
        assertTrue(DeviceCatalog.profiles.all { it.id in presetIds })
        val categoryOrder = DeviceCatalog.presets.map { it.category.ordinal }
        assertEquals(categoryOrder.sorted(), categoryOrder)
    }

    @Test
    fun `picking an icon offers its name unless it is already used`() {
        val ps5 = DeviceCatalog.presets.first { it.id == "ps5" }
        assertEquals("PlayStation 5", ps5.nameToOffer("HDMI 2"))
        assertNull(ps5.nameToOffer("PlayStation 5"))
        assertNull(ps5.nameToOffer(" playstation 5 "))
        assertNull(IconPreset.offerName("  ", "HDMI 1"))
        assertNull(IconPreset.offerName(null, "HDMI 1"))

        val favourite = DeviceCatalog.genericPresets.first { it.id == "generic_star" }
        val initials = DeviceCatalog.genericPresets.first { it.id == "generic_monogram" }
        assertNull(favourite.nameToOffer("HDMI 1"))
        assertNull(initials.nameToOffer("HDMI 1"))
        assertEquals("AV", DeviceCatalog.genericPresets.first { it.id == "generic_av" }.nameToOffer("HDMI 1"))
    }
}
