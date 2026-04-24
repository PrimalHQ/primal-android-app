package net.primal.data.local.db

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

internal const val SQLITE_IN_CLAUSE_CHUNK_SIZE = 900

internal suspend fun <T, R> List<T>.chunkedQuery(
    chunkSize: Int = SQLITE_IN_CLAUSE_CHUNK_SIZE,
    query: suspend (List<T>) -> List<R>,
): List<R> =
    if (size <= chunkSize) {
        query(this)
    } else {
        chunked(chunkSize).flatMap { query(it) }
    }

internal suspend fun <T, K, V> List<T>.chunkedMapQuery(
    chunkSize: Int = SQLITE_IN_CLAUSE_CHUNK_SIZE,
    query: suspend (List<T>) -> Map<K, V>,
): Map<K, V> =
    if (size <= chunkSize) {
        query(this)
    } else {
        buildMap { chunked(chunkSize).forEach { putAll(query(it)) } }
    }

internal fun <T, R> List<T>.chunkedFlowQuery(
    chunkSize: Int = SQLITE_IN_CLAUSE_CHUNK_SIZE,
    query: (List<T>) -> Flow<List<R>>,
): Flow<List<R>> =
    if (size <= chunkSize) {
        query(this)
    } else {
        combine(chunked(chunkSize).map(query)) { arrays -> arrays.flatMap { it } }
    }
