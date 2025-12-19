package net.primal.core.utils

import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.fetchAndUpdate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

inline fun <K, T> Map<K, List<T>>.forEachKey(action: Map<K, List<T>>.(K) -> Unit) {
    for (element in this.keys) action(element)
}

inline fun <T> List<T>.asMapByKey(keyResolver: (T) -> String): Map<String, T> {
    return this.groupBy { keyResolver(it) }.mapValues { it.value.first() }
}

@ExperimentalAtomicApi
fun <T> AtomicReference<List<T>>.add(item: T) = this.fetchAndUpdate { it + item }

@ExperimentalAtomicApi
fun <T> AtomicReference<List<T>>.remove(item: T) = this.fetchAndUpdate { it - item }

@ExperimentalAtomicApi
fun <K, V> AtomicReference<Map<K, V>>.put(key: K, value: V) = this.fetchAndUpdate { it + (key to value) }

@ExperimentalAtomicApi
fun <K, V> AtomicReference<Map<K, V>>.remove(key: K) = this.fetchAndUpdate { it - key }

@ExperimentalAtomicApi
fun <K, V> AtomicReference<Map<K, V>>.getAndClear() = this.fetchAndUpdate { emptyMap() }

fun <T> MutableStateFlow<List<T>>.add(item: T) = this.update { it + item }
fun <T> MutableStateFlow<List<T>>.remove(item: T) = this.update { it - item }
