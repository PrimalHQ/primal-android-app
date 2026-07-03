package net.primal.shared.data.local.db

import androidx.room.RoomDatabase
import androidx.room.migration.Migration

/**
 * Applies the Room configuration shared by every local database (caching, wallet, account) and
 * builds it. Platform factories supply the driver, file location and query dispatcher through
 * [createDatabaseBuilder]; this function layers the cross-platform policy on top of that builder
 * and calls [RoomDatabase.Builder.build].
 *
 * Destructive migration on downgrade is always enabled (all tables are dropped). [migrations] are
 * registered when provided, and [fallbackToDestructiveMigration] governs destructive fallback for
 * the remaining (non-downgrade) migration cases.
 * Every database gets a [DatabaseSpaceReclaimCallback] so destructive migrations VACUUM the file
 * and WAL journals stay bounded via `PRAGMA journal_size_limit`.
 *
 * @param T the [RoomDatabase] subtype to build.
 * @param fallbackToDestructiveMigration when true, recreate the database (dropping its data) if a
 * required migration is missing. Downgrades are destructive regardless of this flag.
 * @param logEngineDiagnostics when true, attaches [PragmaDiagnosticsCallback] to log the live SQLite
 * engine settings on every opened connection. Intended for debug builds only; keep it false in
 * release so production opens no diagnostic logging.
 * @param migrations the [Migration]s to register; an empty list registers none.
 * @param createDatabaseBuilder supplies the platform-specific [RoomDatabase.Builder] (driver, file
 * path, query coroutine context) that this function finishes configuring and builds.
 * @return the configured, built [T] instance.
 */
fun <T : RoomDatabase> buildLocalDatabase(
    fallbackToDestructiveMigration: Boolean,
    logEngineDiagnostics: Boolean = false,
    migrations: List<Migration> = emptyList(),
    createDatabaseBuilder: () -> RoomDatabase.Builder<T>,
): T {
    return createDatabaseBuilder()
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .fallbackToDestructiveMigration(fallbackToDestructiveMigration)
        .addCallback(DatabaseSpaceReclaimCallback())
        .run { if (logEngineDiagnostics) addCallback(PragmaDiagnosticsCallback) else this }
        .run {
            if (migrations.isNotEmpty()) {
                addMigrations(*migrations.toTypedArray())
            } else {
                this
            }
        }
        .build()
}
