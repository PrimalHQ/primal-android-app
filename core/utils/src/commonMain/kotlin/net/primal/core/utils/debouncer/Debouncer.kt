package net.primal.core.utils.debouncer

import kotlin.time.Duration
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.primal.core.utils.Result

abstract class Debouncer {

    private var lastTimeFetched: Instant = Instant.DISTANT_PAST

    private fun canDoWork(duration: Duration): Boolean {
        return lastTimeFetched < Clock.System.now().minus(duration)
    }

    suspend fun invokeImmediately(): Result<Unit> {
        lastTimeFetched = Clock.System.now()
        return doWork()
    }

    suspend fun invokeWithDebounce(duration: Duration): Result<Unit> {
        if (canDoWork(duration)) {
            lastTimeFetched = Clock.System.now()
            return doWork()
        } else {
            return Result.failure(DebounceException())
        }
    }

    protected abstract suspend fun doWork(): Result<Unit>

    class DebounceException : RuntimeException("Work invoked too early. Use `invokeImmediately` to ignore debounce.")
}
