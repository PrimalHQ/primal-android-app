package net.primal.android.core.updater

import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.android.user.updater.UserDataUpdater
import net.primal.android.user.updater.UserDataUpdaterFactory
import net.primal.core.config.AppConfigHandler
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import net.primal.domain.usecase.CreateTsunamiWalletUseCase
import net.primal.domain.usecase.UpdateStaleStreamDataUseCase

@Singleton
class DataUpdater @Inject constructor(
    dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val credentialsStore: CredentialsStore,
    private val userDataSyncerFactory: UserDataUpdaterFactory,
    private val appConfigHandler: AppConfigHandler,
    private val updateStaleStreamDataUseCase: UpdateStaleStreamDataUseCase,
    private val createTsunamiWalletUseCase: CreateTsunamiWalletUseCase,
) {
    private val scope = CoroutineScope(dispatcherProvider.io() + SupervisorJob())
    private var userDataUpdater: UserDataUpdater? = null

    init {
        observeActiveAccount()
    }

    fun updateData() {
        scope.launch { userDataUpdater?.updateWithDebounce(30.minutes) }
        scope.launch { appConfigHandler.updateWithDebounce(30.minutes) }
        scope.launch { updateStaleStreamDataUseCase.updateWithDebounce(30.minutes) }
        scope.launch { createTsunamiWalletIfPossible(userId = activeAccountStore.activeUserId()) }
    }

    private fun observeActiveAccount() =
        scope.launch {
            activeAccountStore.activeUserId.collect { initUserUpdater(activeUserId = it) }
        }

    private fun initUserUpdater(activeUserId: String) {
        userDataUpdater = if (userDataUpdater?.userId != activeUserId) {
            userDataSyncerFactory.create(userId = activeUserId)
        } else {
            userDataUpdater
        }
    }

    private suspend fun createTsunamiWalletIfPossible(userId: String) {
        val credentials = runCatching { credentialsStore.findOrThrow(npub = userId.hexToNpubHrp()) }.getOrNull()
        val tsunamiKey = credentials?.nsec ?: userId
        createTsunamiWalletUseCase.invoke(userId = userId, walletKey = tsunamiKey)
    }
}
