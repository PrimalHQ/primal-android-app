package net.primal.android.config.dynamic

import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.primal.android.config.AppConfigProvider
import net.primal.android.config.store.AppConfigDataStore
import net.primal.android.core.coroutines.CoroutineDispatcherProvider

class DynamicConfigProvider @Inject constructor(
    dispatcherProvider: CoroutineDispatcherProvider,
    private val appConfigStore: AppConfigDataStore,
) : AppConfigProvider {

    private val scope = CoroutineScope(dispatcherProvider.io())

    override suspend fun cacheUrl(): StateFlow<String> = appConfigStore.config.map { it.cacheUrl }.stateIn(scope)

    override suspend fun uploadUrl(): StateFlow<String> = appConfigStore.config.map { it.uploadUrl }.stateIn(scope)

    override suspend fun walletUrl(): StateFlow<String> = appConfigStore.config.map { it.walletUrl }.stateIn(scope)
}
