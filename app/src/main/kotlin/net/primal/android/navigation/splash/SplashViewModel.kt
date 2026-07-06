package net.primal.android.navigation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.core.config.AppConfigHandler
import net.primal.core.utils.getOrDefault
import net.primal.core.utils.onFailure
import net.primal.core.utils.runCatching
import net.primal.domain.feeds.FeedsRepository
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val appConfigHandler: AppConfigHandler,
    private val credentialsStore: CredentialsStore,
    private val feedsRepository: FeedsRepository,
) : ViewModel() {

    private val _isAuthCheckComplete = MutableStateFlow(false)
    val isAuthCheckComplete = _isAuthCheckComplete

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn = _isLoggedIn

    private var started = false

    fun start(prefetchFeeds: Boolean) {
        if (started) return
        started = true
        checkAuthState(prefetchFeeds = prefetchFeeds)
        fetchLatestAppConfig()
    }

    private fun checkAuthState(prefetchFeeds: Boolean) =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            _isLoggedIn.value = userId.isNotEmpty()

            if (prefetchFeeds && userId.isNotEmpty() && isLocalKeyAccount(userId = userId)) {
                prefetchNoteFeeds(userId = userId)
            }

            _isAuthCheckComplete.value = true
        }

    private fun isLocalKeyAccount(userId: String): Boolean =
        runCatching { !credentialsStore.isExternalSignerCredential(npub = userId.hexToNpubHrp()) }
            .getOrDefault(false)

    private suspend fun prefetchNoteFeeds(userId: String) =
        runCatching {
            withTimeoutOrNull(FEEDS_PREFETCH_TIMEOUT) {
                feedsRepository.fetchAndPersistNoteFeeds(userId = userId)
            } ?: Napier.w { "Note feeds prefetch timed out during splash." }
        }.onFailure { error ->
            Napier.w(throwable = error) { "Failed to prefetch note feeds during splash." }
        }

    private fun fetchLatestAppConfig() =
        viewModelScope.launch {
            appConfigHandler.updateImmediately()
        }

    companion object {
        private val FEEDS_PREFETCH_TIMEOUT = 2.seconds
    }
}
