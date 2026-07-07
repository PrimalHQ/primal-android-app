package net.primal.android.migration

const val CURRENT_APP_VERSION = 0

/**
 * An app-global migration step, run once per device at startup.
 *
 * Implementations must be consecutive ([endVersion] == [startVersion] + 1) and
 * idempotent: [migrate] may run on a fresh install or re-run after a later step
 * fails, so avoid anything that breaks when applied twice. It runs on the splash
 * path with no timeout, so keep it fast.
 *
 * Register via `@Provides @IntoSet` in AppMigrationModule and bump [CURRENT_APP_VERSION].
 */
interface AppMigration {
    val startVersion: Int
    val endVersion: Int
    suspend fun migrate()
}

/**
 * Validates the registered chain: every step consecutive, no duplicate start
 * versions, and exactly [targetVersion] migrations covering `0 until targetVersion`.
 * Throws [IllegalArgumentException] on any violation.
 */
fun validateMigrationChain(migrations: Set<AppMigration>, targetVersion: Int) {
    migrations.forEach {
        require(it.endVersion == it.startVersion + 1) {
            "Migration ${it.startVersion} -> ${it.endVersion} is not strictly consecutive."
        }
    }
    val startVersions = migrations.map { it.startVersion }
    require(startVersions.toSet().size == startVersions.size) {
        "Duplicate startVersion detected among registered migrations."
    }
    require(migrations.size == targetVersion) {
        "Expected $targetVersion migrations to reach CURRENT_APP_VERSION but found ${migrations.size}."
    }
    for (version in 0 until targetVersion) {
        require(version in startVersions) {
            "No AppMigration registered from version $version."
        }
    }
}
