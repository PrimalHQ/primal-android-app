package net.primal.android.notes.feed.note.ui.attachment

import kotlin.math.floor
import kotlin.math.max

private const val SECONDS_PER_HOUR = 3600
private const val SECONDS_PER_MINUTE = 60

internal fun formatVideoDuration(seconds: Double): String {
    val total = max(0, floor(seconds).toInt())
    val hours = total / SECONDS_PER_HOUR
    val minutes = (total % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val secs = total % SECONDS_PER_MINUTE
    return if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
    } else {
        "$minutes:${secs.toString().padStart(2, '0')}"
    }
}
