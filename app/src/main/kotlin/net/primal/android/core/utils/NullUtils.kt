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
