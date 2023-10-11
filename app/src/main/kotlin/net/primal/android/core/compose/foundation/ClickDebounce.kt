package net.primal.android.core.compose.foundation

class ClickDebounce(private val timeoutMillis: Long = 500L) {
    private val now: Long get() = System.currentTimeMillis()

    private var lastEventTimeInMillis: Long = 0

    fun processEvent(event: () -> Unit) {
        if (now - lastEventTimeInMillis >= timeoutMillis) {
            event.invoke()
        }
        lastEventTimeInMillis = now
    }
}
