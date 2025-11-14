package net.primal.android.core.compose

import android.icu.text.SimpleDateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.Date
import java.util.Locale

private const val SECONDS_TO_MILLIS = 1000L

@Composable
fun rememberFormattedDateTime(timestamp: Long): String {
    return remember(timestamp) {
        val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())
        simpleDateFormat.format(Date(timestamp * SECONDS_TO_MILLIS))
    }
}
