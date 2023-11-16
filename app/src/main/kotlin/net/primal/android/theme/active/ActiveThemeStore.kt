package net.primal.android.theme.active

import androidx.datastore.core.DataStore
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import net.primal.android.theme.active.di.ActiveThemeDataStore
import net.primal.android.theme.domain.PrimalTheme

@Singleton
class ActiveThemeStore @Inject constructor(
    @ActiveThemeDataStore private val persistence: DataStore<String>,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    val userThemeState: StateFlow<PrimalTheme?> = persistence.data
        .map { PrimalTheme.valueOf(themeName = it) }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = initialValue(),
        )

    private fun initialValue(): PrimalTheme? =
        runBlocking {
            PrimalTheme.valueOf(themeName = persistence.data.first())
        }

    suspend fun setUserTheme(theme: String) {
        persistence.updateData { theme }
    }
}
