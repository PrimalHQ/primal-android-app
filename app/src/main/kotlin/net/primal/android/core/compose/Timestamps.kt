package net.primal.android.core.compose

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import net.primal.android.R

@Composable
fun Instant.asBeforeNowFormat(shortFormat: Boolean = true): String {
    val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()
    val diff = ChronoUnit.SECONDS.between(this, now)

    val minute = 60
    val hour = minute * 60
    val day = hour * 24
    val week = day * 7
    val month = day * 30
    val year = day * 365

    return when {
        diff < minute -> stringResource(R.string.timestamp_since_just_now)

        diff < hour -> (diff / minute).format(
            shortResId = R.string.timestamp_since_minutes,
            longResId = R.plurals.timestamp_since_minutes,
            shortFormat = shortFormat,
        )

        diff < day -> (diff / hour).format(
            shortResId = R.string.timestamp_since_hours,
            longResId = R.plurals.timestamp_since_hours,
            shortFormat = shortFormat,
        )

        diff < week -> (diff / day).format(
            shortResId = R.string.timestamp_since_days,
            longResId = R.plurals.timestamp_since_days,
            shortFormat = shortFormat,
        )

        diff < month -> (diff / week).format(
            shortResId = R.string.timestamp_since_weeks,
            longResId = R.plurals.timestamp_since_weeks,
            shortFormat = shortFormat,
        )

        diff < year -> (diff / month).format(
            shortResId = R.string.timestamp_since_months,
            longResId = R.plurals.timestamp_since_months,
            shortFormat = shortFormat,
        )

        else -> (diff / year).format(
            shortResId = R.string.timestamp_since_years,
            longResId = R.plurals.timestamp_since_years,
            shortFormat = shortFormat,
        )
    }
}

@Composable
private fun Long.format(
    @StringRes shortResId: Int,
    @PluralsRes longResId: Int,
    shortFormat: Boolean,
) = if (shortFormat) {
    stringResource(shortResId, "$this")
} else {
    pluralStringResource(longResId, this.toInt(), "$this")
}
