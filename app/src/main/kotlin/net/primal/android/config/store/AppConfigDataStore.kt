package net.primal.android.config.store

import androidx.datastore.core.DataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import net.primal.android.config.domain.AppConfig

@Singleton
class AppConfigDataStore @Inject constructor(
    private val persistence: DataStore<AppConfig>,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    val config = persistence.data
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = runBlocking { persistence.data.first() },
        )

    suspend fun updateConfig(reducer: AppConfig.() -> AppConfig) {
        persistence.updateData { it.reducer() }
    }
}
