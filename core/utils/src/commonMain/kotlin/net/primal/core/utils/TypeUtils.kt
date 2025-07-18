package net.primal.core.utils

inline fun <reified T, V> Any?.getIfTypeOrNull(reducer: T.() -> V): V? =
    if (this is T) {
        this.reducer()
    } else {
        null
    }
