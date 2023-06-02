package net.primal.android.core

inline fun <K, T> Map<K, List<T>>.forEachKey(action: Map<K, List<T>>.(K) -> Unit) {
    for (element in this.keys) action(element)
}