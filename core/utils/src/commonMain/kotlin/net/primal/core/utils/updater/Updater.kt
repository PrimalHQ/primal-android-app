package net.primal.core.utils.updater

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant
import net.primal.core.utils.Result

abstract class Updater {

    private var lastTimeFetched: Instant = Instant.DISTANT_PAST

    private fun canUpdate(duration: Duration): Boolean {
        return lastTimeFetched < Clock.System.now().minus(duration)
    }

    suspend fun updateImmediately(): Result<Unit> {
        lastTimeFetched = Clock.System.now()
        return doUpdate()
    }

    suspend fun updateWithDebounce(duration: Duration): Result<Unit> {
        if (canUpdate(duration)) {
            lastTimeFetched = Clock.System.now()
            return doUpdate()
        } else {
            return Result.failure(DebounceException())
        }
    }

    protected abstract suspend fun doUpdate(): Result<Unit>

    class DebounceException : RuntimeException("Work invoked too early. Use `invokeImmediately` to ignore debounce.")
}
