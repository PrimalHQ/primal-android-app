package net.primal.android.core.ext

inline fun <K, T> Map<K, List<T>>.forEachKey(action: Map<K, List<T>>.(K) -> Unit) {
    for (element in this.keys) action(element)
}

inline fun <T> List<T>.asMapByKey(keyResolver: (T) -> String): Map<String, T> {
    return this.groupBy { keyResolver(it) }.mapValues { it.value.first() }
}
