package net.primal.android.premium.manage.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.nostr.notary.MissingPrivateKeyException
import net.primal.android.premium.manage.contact.PremiumContactListContract.SideEffect
import net.primal.android.premium.manage.contact.PremiumContactListContract.UiEvent
import net.primal.android.premium.manage.contact.PremiumContactListContract.UiState
import net.primal.android.premium.manage.contact.model.FollowListBackup
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.core.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel
class PremiumContactListViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val premiumRepository: PremiumRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    init {
        fetchContactsLists()
        observeEvents()
    }

    private fun fetchContactsLists() {
        viewModelScope.launch {
            setState { copy(fetching = true) }
            try {
                val userId = activeAccountStore.activeUserId()
                val followLists = premiumRepository.fetchRecoveryContactsList(userId = userId)
                val backups = followLists.map {
                    FollowListBackup(
                        event = it,
                        timestamp = it.createdAt,
                        followsCount = it.tags.size,
                    )
                }.sortedByDescending { it.timestamp }
                setState { copy(backups = backups) }
            } catch (error: MissingPrivateKeyException) {
                Timber.e(error)
            } catch (error: WssException) {
                Timber.e(error)
            } finally {
                setState { copy(fetching = false) }
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.RecoverFollowList -> recoverFollowList(backup = it.backup)
                }
            }
        }
    }

    private fun recoverFollowList(backup: FollowListBackup) {
        viewModelScope.launch {
            try {
                userRepository.recoverFollowList(
                    userId = activeAccountStore.activeUserId(),
                    tags = backup.event.tags,
                    content = backup.event.content,
                )
                setEffect(SideEffect.RecoverSuccessful)
            } catch (_: Exception) {
                setEffect(SideEffect.RecoverFailed)
            }
        }
    }
}
