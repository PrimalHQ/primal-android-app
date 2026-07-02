package net.primal.android.migration

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** In-memory [DataStore] of String for driving [AppMigrationStore] without Android/Robolectric. */
class InMemoryStringDataStore(initial: String = "") : DataStore<String> {
    private val state = MutableStateFlow(initial)
    override val data: Flow<String> = state
    override suspend fun updateData(transform: suspend (t: String) -> String): String {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

/** Records how many times [migrate] ran; optionally fails via [onMigrate]. */
class FakeAppMigration(
    override val startVersion: Int,
    override val endVersion: Int,
    private val onMigrate: suspend () -> Unit = {},
) : AppMigration {
    var runCount = 0
        private set

    override suspend fun migrate() {
        runCount++
        onMigrate()
    }
}
