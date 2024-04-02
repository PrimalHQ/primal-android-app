package net.primal.android.core.utils

import java.time.Instant
import kotlin.time.Duration

fun Instant?.isOlderThan(duration: Duration): Boolean {
    if (this == null) return true
    return this < Instant.now().minusSeconds(duration.inWholeSeconds)
}
