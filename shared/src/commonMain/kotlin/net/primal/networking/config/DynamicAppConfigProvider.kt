package net.primal.networking.config

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.primal.core.coroutines.DispatcherProvider
import net.primal.networking.config.store.AppConfigDataStore

internal class DynamicAppConfigProvider(
    dispatcherProvider: DispatcherProvider,
    private val appConfigStore: AppConfigDataStore,
) : AppConfigProvider {

    private val scope = CoroutineScope(dispatcherProvider.io())

    override suspend fun cacheUrl(): StateFlow<String> = appConfigStore.config.map { it.cacheUrl }.stateIn(scope)

    override suspend fun uploadUrl(): StateFlow<String> = appConfigStore.config.map { it.uploadUrl }.stateIn(scope)

    override suspend fun walletUrl(): StateFlow<String> = appConfigStore.config.map { it.walletUrl }.stateIn(scope)
}
