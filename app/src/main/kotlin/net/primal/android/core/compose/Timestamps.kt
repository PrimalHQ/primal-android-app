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

private const val MINUTE = 60
private const val HOUR = MINUTE * 60
private const val DAY = HOUR * 24
private const val WEEK = DAY * 7
private const val MONTH = DAY * 30
private const val YEAR = DAY * 365

@Composable
fun Instant.asBeforeNowFormat(shortFormat: Boolean = true): String {
    val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()
    val diff = ChronoUnit.SECONDS.between(this, now)

    return when {
        diff < MINUTE -> stringResource(R.string.timestamp_since_just_now)

        diff < HOUR -> (diff / MINUTE).format(
            shortResId = R.string.timestamp_since_minutes,
            longResId = R.plurals.timestamp_since_minutes,
            shortFormat = shortFormat,
        )

        diff < DAY -> (diff / HOUR).format(
            shortResId = R.string.timestamp_since_hours,
            longResId = R.plurals.timestamp_since_hours,
            shortFormat = shortFormat,
        )

        diff < WEEK -> (diff / DAY).format(
            shortResId = R.string.timestamp_since_days,
            longResId = R.plurals.timestamp_since_days,
            shortFormat = shortFormat,
        )

        diff < MONTH -> (diff / WEEK).format(
            shortResId = R.string.timestamp_since_weeks,
            longResId = R.plurals.timestamp_since_weeks,
            shortFormat = shortFormat,
        )

        diff < YEAR -> (diff / MONTH).format(
            shortResId = R.string.timestamp_since_months,
            longResId = R.plurals.timestamp_since_months,
            shortFormat = shortFormat,
        )

        else -> (diff / YEAR).format(
            shortResId = R.string.timestamp_since_years,
            longResId = R.plurals.timestamp_since_years,
            shortFormat = shortFormat,
        )
    }
}

@Composable
fun Instant.asFromNowFormat(shortFormat: Boolean = false): String {
    val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()
    val diff = ChronoUnit.SECONDS.between(now, this)

    return when {
        diff < MINUTE -> stringResource(id = R.string.timestamp_in_less_than_a_minute)

        diff < HOUR -> (diff / MINUTE).format(
            shortResId = R.string.timestamp_in_minutes,
            longResId = R.plurals.timestamp_in_minutes,
            shortFormat = shortFormat,
        )

        diff < DAY -> (diff / HOUR).format(
            shortResId = R.string.timestamp_in_hours,
            longResId = R.plurals.timestamp_in_hours,
            shortFormat = shortFormat,
        )

        diff < WEEK -> (diff / DAY).format(
            shortResId = R.string.timestamp_in_days,
            longResId = R.plurals.timestamp_in_days,
            shortFormat = shortFormat,
        )

        diff < MONTH -> (diff / WEEK).format(
            shortResId = R.string.timestamp_in_weeks,
            longResId = R.plurals.timestamp_in_weeks,
            shortFormat = shortFormat,
        )

        diff < YEAR -> (diff / MONTH).format(
            shortResId = R.string.timestamp_in_months,
            longResId = R.plurals.timestamp_in_months,
            shortFormat = shortFormat,
        )

        else -> (diff / YEAR).format(
            shortResId = R.string.timestamp_in_years,
            longResId = R.plurals.timestamp_in_years,
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
