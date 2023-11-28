package net.primal.android.config.store

import androidx.datastore.core.DataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import net.primal.android.config.domain.AppConfig
import net.primal.android.core.coroutines.CoroutineDispatcherProvider

@Singleton
class AppConfigDataStore @Inject constructor(
    dispatcherProvider: CoroutineDispatcherProvider,
    private val persistence: DataStore<AppConfig>,
) {

    private val scope = CoroutineScope(dispatcherProvider.io())

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
