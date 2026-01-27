package net.primal.android.wallet.init

import androidx.annotation.Keep
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.connections.nostr.NwcService
import timber.log.Timber

@Keep
@Singleton
class NwcWalletLifecycleInitializer @Inject constructor(
    dispatchers: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val nwcService: NwcService,
) {

    private val scope = CoroutineScope(dispatchers.io())

    private var currentUserId: String? = null

    fun start() {
        scope.launch {
            activeAccountStore.activeUserId
                .map { it.takeIf { id -> id.isNotBlank() } }
                .distinctUntilChanged()
                .collect { userIdOrNull ->
                    currentUserId?.let {
                        Timber.d("NwcService destroying for previous user")
                        nwcService.destroy()
                        currentUserId = null
                    }

                    val userId = userIdOrNull ?: return@collect

                    Timber.d("NwcService initializing for userId=%s", userId)
                    nwcService.initialize(userId)
                    currentUserId = userId
                }
        }
    }
}
