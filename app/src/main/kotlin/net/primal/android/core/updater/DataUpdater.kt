package net.primal.android.core.updater

import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.updater.UserDataUpdater
import net.primal.android.user.updater.UserDataUpdaterFactory
import net.primal.core.config.AppConfigHandler
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.usecase.UpdateStaleStreamDataUseCase

class DataUpdater @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val userDataSyncerFactory: UserDataUpdaterFactory,
    private val appConfigHandler: AppConfigHandler,
    private val updateStaleStreamDataUseCase: UpdateStaleStreamDataUseCase,
) {
    private val scope = CoroutineScope(dispatcherProvider.io() + SupervisorJob())
    private var userDataUpdater: UserDataUpdater? = null

    init {
        observeActiveAccount()
    }

    fun updateData() =
        scope.launch {
            userDataUpdater?.invokeWithDebounce(30.minutes)
            appConfigHandler.invokeWithDebounce(30.minutes)
            updateStaleStreamDataUseCase.invokeWithDebounce(30.minutes)
        }

    private fun observeActiveAccount() =
        scope.launch {
            activeAccountStore.activeUserAccount.collect { initUserUpdater(activeUserId = it.pubkey) }
        }

    private fun initUserUpdater(activeUserId: String) {
        userDataUpdater = if (userDataUpdater?.userId != activeUserId) {
            userDataSyncerFactory.create(userId = activeUserId)
        } else {
            userDataUpdater
        }
    }
}
