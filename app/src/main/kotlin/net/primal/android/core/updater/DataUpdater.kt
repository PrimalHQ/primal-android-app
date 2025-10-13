package net.primal.android.core.updater

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.updater.UserDataUpdater
import net.primal.android.user.updater.UserDataUpdaterFactory
import net.primal.core.config.AppConfigHandler
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.usecase.UpdateStaleStreamDataUseCase

@Singleton
class DataUpdater @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val userDataSyncerFactory: UserDataUpdaterFactory,
    private val appConfigHandler: AppConfigHandler,
    private val updateStaleStreamDataUseCase: UpdateStaleStreamDataUseCase,
) {
    private val scope = CoroutineScope(dispatcherProvider.io() + SupervisorJob())
    private val _userDataUpdater = MutableStateFlow<UserDataUpdater?>(null)
    private val userDataUpdater = _userDataUpdater.asStateFlow()

    init {
        observeActiveAccount()
    }

    fun updateData() =
        scope.launch {
            val updater = userDataUpdater.first { it != null }

            updater?.updateWithDebounce(30.minutes)
            appConfigHandler.updateWithDebounce(30.minutes)
            updateStaleStreamDataUseCase.updateWithDebounce(30.minutes)
        }

    private fun observeActiveAccount() =
        scope.launch {
            activeAccountStore.activeUserAccount.collect { initUserUpdater(activeUserId = it.pubkey) }
        }

    private fun initUserUpdater(activeUserId: String) {
        if (activeUserId.isBlank()) {
            _userDataUpdater.value = null
            return
        }

        if (_userDataUpdater.value?.userId != activeUserId) {
            _userDataUpdater.value = userDataSyncerFactory.create(userId = activeUserId)
        }
    }
}
