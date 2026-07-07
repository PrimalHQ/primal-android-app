package net.primal.android.migration

import androidx.datastore.core.DataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import net.primal.android.migration.di.AppMigrationVersionDataStore

@Singleton
class AppMigrationStore @Inject constructor(
    @AppMigrationVersionDataStore private val persistence: DataStore<String>,
) {
    suspend fun currentVersion(): Int =
        persistence.data.first().toIntOrNull() ?: 0

    suspend fun setVersion(version: Int) =
        persistence.updateData { version.toString() }
}
