package net.primal.android.settings.muted.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.settings.muted.list.MutedSettingsContract.UiEvent
import net.primal.android.settings.muted.list.MutedSettingsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.profile.MutedUserRepository
import timber.log.Timber

@HiltViewModel
class MutedSettingsViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val mutedUserRepository: MutedUserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchLatestMuteList()
        observeMutedUsers()
        observeEvents()
    }

    private fun observeMutedUsers() =
        viewModelScope.launch {
            mutedUserRepository.observeMutedUsersByOwnerId(ownerId = activeAccountStore.activeUserId()).collect {
                setState {
                    copy(mutedUsers = it.map { it.asProfileDetailsUi() })
                }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.UnmuteEvent -> unmuteEventHandler(it)
                    is UiEvent.MuteHashtags -> {}
                    is UiEvent.MuteThreads -> {}
                    is UiEvent.MuteWord -> {}
                    is UiEvent.UnmuteHashtags -> {}
                    is UiEvent.UnmuteThreads -> {}
                    is UiEvent.UnmuteUser -> {}
                    is UiEvent.UnmuteWord -> {}
                }
            }
        }

    private fun fetchLatestMuteList() =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    mutedUserRepository.fetchAndPersistMuteList(
                        userId = activeAccountStore.activeUserId(),
                    )
                }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun unmuteEventHandler(event: UiEvent.UnmuteEvent) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    mutedUserRepository.unmuteUserAndPersistMuteList(
                        userId = activeAccountStore.activeUserId(),
                        unmutedUserId = event.pubkey,
                    )
                }
            } catch (error: WssException) {
                setState {
                    copy(error = UiState.MutedSettingsError.FailedToUnmuteUserError(error))
                }
            } catch (error: SignatureException) {
                Timber.w(error)
            } catch (error: NostrPublishException) {
                setState {
                    copy(error = UiState.MutedSettingsError.FailedToUnmuteUserError(error))
                }
            }
        }
}
