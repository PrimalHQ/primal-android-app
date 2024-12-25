package net.primal.android.core.utils

inline fun <U, V> ifNotNull(
    u: U?,
    v: V?,
    block: (U, V) -> Unit,
) {
    if (u != null && v != null) {
        block(u, v)
    }
}

inline fun <U, V, T> ifNotNull(
    u: U?,
    v: V?,
    t: T?,
    block: (U, V, T) -> Unit,
) {
    if (u != null && v != null && t != null) {
        block(u, v, t)
    }
}

fun assertNotNullCount(
    vararg args: Any?,
    atMost: Int = Int.MAX_VALUE,
    atLeast: Int = Int.MIN_VALUE,
    exactly: Int? = null,
    message: () -> Any,
) {
    args.sumOf { (it != null).toInt() }.apply {
        if (this > atMost || this <= atLeast) {
            error(message = message())
        }

        if (exactly != null && this != exactly) {
            error(message = message())
        }
    }
}

private fun Boolean.toInt() = if (this) 1 else 0
