package dev.germar.sourcelauncher.ui.components

import android.content.Context
import android.text.format.DateFormat
import dev.germar.sourcelauncher.model.ClockFormat
import java.text.SimpleDateFormat

/** Formats a time of day as chosen in settings: like the system, 12-hour with AM/PM, or 24-hour. */
fun clockFormatter(context: Context, format: ClockFormat): java.text.DateFormat {
    val locale = context.resources.configuration.locales[0]
    val uses24Hour = when (format) {
        ClockFormat.SYSTEM -> DateFormat.is24HourFormat(context)
        ClockFormat.H12 -> false
        ClockFormat.H24 -> true
    }
    val skeleton = if (uses24Hour) "Hm" else "hma"
    return SimpleDateFormat(DateFormat.getBestDateTimePattern(locale, skeleton), locale)
}
