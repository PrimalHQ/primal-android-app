package net.primal.android.settings.muted

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import javax.inject.Inject

@HiltViewModel
class MutedSettingsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val mutedUserRepository: MutedUserRepository
) : ViewModel() {
    private val _state = MutableStateFlow(MutedSettingsContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: MutedSettingsContract.UiState.() -> MutedSettingsContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private val _event = MutableSharedFlow<MutedSettingsContract.UiEvent>()
    fun setEvent(event: MutedSettingsContract.UiEvent) = viewModelScope.launch {
        _event.emit(event)
    }

    init {
        observeMutedUsers()
        observeEvents()
    }

    private fun observeMutedUsers() = viewModelScope.launch {
        mutedUserRepository.mutedUsers.collect {
            setState { copy(mutelist = it) }
        }
    }

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is MutedSettingsContract.UiEvent.RemovedFromMuteListEvent -> removedFromMutelistEventHandler(
                    it
                )
            }
        }
    }

    private suspend fun removedFromMutelistEventHandler(event: MutedSettingsContract.UiEvent.RemovedFromMuteListEvent) {
        try {
            mutedUserRepository.unmuteUserAndPersistMutelist(
                userId = activeAccountStore.activeUserId(),
                unmutedUserPubkey = event.pubkey
            )
        } catch (error: Exception) {
            when (error) {
                is NostrPublishException,
                is WssException ->
                    setState {
                        copy(
                            error = MutedSettingsContract.UiState.MutedSettingsError.FailedToUnmuteUserError(
                                error = error
                            )
                        )
                    }
            }
        }
    }
}