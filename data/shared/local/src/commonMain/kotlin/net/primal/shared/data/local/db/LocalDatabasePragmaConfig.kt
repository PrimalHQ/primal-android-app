package net.primal.shared.data.local.db

/**
 * Per-connection SQLite `PRAGMA` settings applied to a local database through [PragmaConfigCallback].
 *
 * Each property is optional: `null` leaves the corresponding `PRAGMA` at its Room/SQLite default,
 * while a non-null value is applied on every connection the database opens. Nullability is required
 * because `0` is a valid value for several of these settings and therefore cannot signal "unset".
 *
 * `journal_mode`, `synchronous` and `busy_timeout` are intentionally not represented here; Room
 * configures them per connection and they must not be overridden.
 *
 * @property journalSizeLimitBytes maximum size, in bytes, that the `-wal` file is truncated to after
 * a checkpoint (`PRAGMA journal_size_limit`), bounding write-ahead log growth on disk.
 * @property cacheSizeKib page-cache size per connection, in KiB, applied as a negative
 * `PRAGMA cache_size`. Applied to each connection in the pool.
 * @property mmapSizeBytes upper bound for memory-mapped I/O, in bytes (`PRAGMA mmap_size`).
 * @property tempStoreMemory when `true`, sets `PRAGMA temp_store = MEMORY` so transient indices and
 * sorts are kept in memory instead of spilling to temporary files.
 */
data class LocalDatabasePragmaConfig(
    val journalSizeLimitBytes: Long? = null,
    val cacheSizeKib: Int? = null,
    val mmapSizeBytes: Long? = null,
    val tempStoreMemory: Boolean = false,
) {
    companion object {
        /**
         * Tuning for the high-traffic caching database (feeds/notes/profiles).
         */
        val CACHING = LocalDatabasePragmaConfig(
            // 8 MiB. SQLite default: -1 (no limit — the -wal file is never size-capped).
//            journalSizeLimitBytes = 8L * 1024 * 1024,
            // 4 MiB per connection. SQLite default: -2000 (~2 MiB per connection).
//            cacheSizeKib = 4 * 1024,
            // 128 MiB. SQLite default: 0 (memory-mapping disabled).
//            mmapSizeBytes = 128L * 1024 * 1024,
            // temp_store = MEMORY. Default: false -> temp_store = 0 (DEFAULT, i.e. on-disk temp files).
//            tempStoreMemory = true,
        )
    }
}
