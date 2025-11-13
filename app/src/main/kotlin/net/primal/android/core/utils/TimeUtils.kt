package net.primal.android.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.time.Duration

fun Instant?.isOlderThan(duration: Duration): Boolean {
    if (this == null) return true
    return this < Instant.now().minusSeconds(duration.inWholeSeconds)
}

fun Instant.formatToDefaultDateTimeFormat(dateTimeStyle: FormatStyle): String {
    val zoneId: ZoneId = ZoneId.systemDefault()
    val locale: Locale = Locale.getDefault()

    val formatter: DateTimeFormatter = DateTimeFormatter
        .ofLocalizedDateTime(dateTimeStyle)
        .withLocale(locale)

    return formatter.format(this.atZone(zoneId))
}

fun Instant.formatToDefaultDateFormat(dateStyle: FormatStyle): String {
    val zoneId: ZoneId = ZoneId.systemDefault()
    val locale: Locale = Locale.getDefault()

    val formatter: DateTimeFormatter = DateTimeFormatter
        .ofLocalizedDate(dateStyle)
        .withLocale(locale)

    return formatter.format(this.atZone(zoneId))
}

fun Instant.formatToDefaultTimeFormat(dateStyle: FormatStyle): String {
    val zoneId: ZoneId = ZoneId.systemDefault()
    val locale: Locale = Locale.getDefault()

    val formatter: DateTimeFormatter = DateTimeFormatter
        .ofLocalizedTime(dateStyle)
        .withLocale(locale)

    return formatter.format(this.atZone(zoneId))
}

private const val SECONDS_TO_MILLIS = 1000L

object PrimalDateFormats {
    const val DATETIME_MM_DD_YYYY_HH_MM_A = "MMM dd, yyyy h:mm a"
    const val DATETIME_MM_DD_YYYY_HH_MM_SS_A = "MMM dd, yyyy h:mm:ss a"
}

@Composable
fun rememberPrimalFormattedDateTime(timestamp: Long, format: String): String {
    return remember(timestamp, format) {
        val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
        simpleDateFormat.format(Date(timestamp * SECONDS_TO_MILLIS))
    }
}
