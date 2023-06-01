package net.primal.android.theme.active

import androidx.datastore.core.DataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import net.primal.android.theme.PrimalTheme
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveThemeStore @Inject constructor(
    private val persistence: DataStore<String>,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    val userThemeState = persistence.data.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = runBlocking { persistence.data.first() },
    ).map {
        PrimalTheme.valueOf(themeName = it)
    }

    suspend fun setUserTheme(theme: String) {
        persistence.updateData { theme }
    }

    suspend fun clearTheme() {
        persistence.updateData { "" }
    }

}
