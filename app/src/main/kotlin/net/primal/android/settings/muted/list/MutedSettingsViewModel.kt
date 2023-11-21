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
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.authorNameUiFriendly
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.settings.muted.db.MutedUser
import net.primal.android.settings.muted.list.MutedSettingsContract.UiEvent
import net.primal.android.settings.muted.list.MutedSettingsContract.UiState
import net.primal.android.settings.muted.list.model.MutedUserUi
import net.primal.android.settings.muted.repository.MutedUserRepository
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class MutedSettingsViewModel @Inject constructor(
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
            mutedUserRepository.observeMutedUsers().collect {
                setState {
                    copy(mutedUsers = it.map { it.asMutedUserUi() })
                }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.UnmuteEvent -> unmuteEventHandler(it)
                }
            }
        }

    private fun fetchLatestMuteList() =
        viewModelScope.launch {
            mutedUserRepository.fetchAndPersistMuteList(
                userId = activeAccountStore.activeUserId(),
            )
        }

    private suspend fun unmuteEventHandler(event: UiEvent.UnmuteEvent) =
        viewModelScope.launch {
            try {
                mutedUserRepository.unmuteUserAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                    unmutedUserId = event.pubkey,
                )
            } catch (error: WssException) {
                setState {
                    copy(error = UiState.MutedSettingsError.FailedToUnmuteUserError(error))
                }
            } catch (error: NostrPublishException) {
                setState {
                    copy(error = UiState.MutedSettingsError.FailedToUnmuteUserError(error))
                }
            }
        }

    private fun MutedUser.asMutedUserUi() =
        MutedUserUi(
            displayName = this.profileData?.authorNameUiFriendly()
                ?: this.mutedAccount.userId.asEllipsizedNpub(),
            userId = this.mutedAccount.userId,
            avatarCdnImage = this.profileData?.avatarCdnImage,
            internetIdentifier = this.profileData?.internetIdentifier,
        )
}
