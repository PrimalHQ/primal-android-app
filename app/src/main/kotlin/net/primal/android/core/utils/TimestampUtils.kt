package net.primal.android.core.utils

import android.content.res.Resources
import net.primal.android.R
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun Instant.asBeforeNowFormat(res: Resources): String {
    val now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()
    val diff = ChronoUnit.SECONDS.between(this, now)

    val minute = 60
    val hour = minute * 60
    val day = hour * 24
    val week = day * 7
    val month = day * 30
    val year = day * 365

    return when {
        diff < minute -> res.getString(R.string.timestamp_since_just_now)
        diff < hour -> res.getString(R.string.timestamp_since_minutes, "${diff / minute}")
        diff < day -> res.getString(R.string.timestamp_since_hours, "${diff / hour}")
        diff < week -> res.getString(R.string.timestamp_since_days, "${diff / day}")
        diff < month -> res.getString(R.string.timestamp_since_weeks,"${diff / week}")
        diff < year -> res.getString(R.string.timestamp_since_months, "${diff / month}")
        else -> res.getString(R.string.timestamp_since_years, "${diff / year}")
    }
}
