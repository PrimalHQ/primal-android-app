package net.primal.android.migration

import io.github.aakira.napier.Napier
import javax.inject.Inject
import javax.inject.Singleton
import net.primal.android.migration.di.CurrentAppVersion
import net.primal.core.utils.onFailure
import net.primal.core.utils.runCatching

/**
 * Runs pending [AppMigration]s once at startup, persisting the version after each
 * step so a failure resumes from the last good version on the next launch. Failures
 * are logged and stop the chain rather than propagating (cancellation aside).
 */
@Singleton
class AppMigrationRunner @Inject constructor(
    migrations: Set<@JvmSuppressWildcards AppMigration>,
    private val versionStore: AppMigrationStore,
    @CurrentAppVersion private val targetVersion: Int,
) {
    private val byStart = migrations.associateBy { it.startVersion }

    suspend fun runPendingMigrations() {
        var version = 0
        runCatching {
            version = versionStore.currentVersion()
            while (version < targetVersion) {
                val step = byStart[version]
                if (step == null) {
                    Napier.e { "No AppMigration registered from version $version; stopping chain." }
                    return@runCatching
                }
                step.migrate()
                version = step.endVersion
                versionStore.setVersion(version)
            }
        }.onFailure { error ->
            Napier.e(throwable = error) {
                "App migration failed at version $version; will retry next launch."
            }
        }
    }
}
