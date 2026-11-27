package dev.germar.sourcelauncher.catalog

import java.text.Normalizer
import java.util.Locale

/** Recognises devices from the labels a TV reports for its inputs (HDMI-CEC names, custom labels). */
object DeviceMatcher {

    private val trademarkSigns = Regex("\\((r|tm|c)\\)|[®™©]", RegexOption.IGNORE_CASE)
    private val diacritics = Regex("\\p{Mn}+")
    private val separators = Regex("[^a-z0-9]+")
    private val portOnlyLabel = Regex("^(hdmi|av|input|source|port|component|composite|scart|vga|dvi|usb|displayport|dp|ypbpr|cvbs|playback( ?device)?)( ?\\d+)?( ?(arc|earc))?$")

    /**
     * Lower-cases the label, strips trademark signs and accents and collapses everything that is
     * not a letter or digit into single spaces: "PlayStation®5" becomes "playstation5".
     */
    fun normalize(label: String): String {
        val withoutTrademarks = trademarkSigns.replace(label, "")
        val decomposed = Normalizer.normalize(withoutTrademarks, Normalizer.Form.NFD)
        val ascii = diacritics.replace(decomposed, "")
        return separators.replace(ascii.lowercase(Locale.ROOT), " ").trim()
    }

    /** True for labels that only name a port ("HDMI 2", "AV", "Playback 1") rather than a device. */
    fun isGenericPortLabel(label: String): Boolean {
        val normalized = normalize(label)
        return normalized.isEmpty() || portOnlyLabel.matches(normalized)
    }

    fun match(label: String): DeviceProfile? {
        val normalized = normalize(label)
        if (normalized.isEmpty()) return null
        return DeviceCatalog.profiles.firstOrNull { profile ->
            profile.patterns.any { it.containsMatchIn(normalized) }
        }
    }

    /** Returns the first recognised device, trying [labels] in order of trust. */
    fun matchFirst(labels: List<String?>): DeviceProfile? =
        labels.asSequence().filterNotNull().mapNotNull { match(it) }.firstOrNull()
}
