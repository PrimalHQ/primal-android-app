package net.primal.android.premium.manage.content

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
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.manage.content.PremiumContentBackupContract.SideEffect
import net.primal.android.premium.manage.content.PremiumContentBackupContract.UiEvent
import net.primal.android.premium.manage.content.PremiumContentBackupContract.UiState
import net.primal.android.premium.manage.content.model.ContentGroup
import net.primal.android.premium.manage.content.model.ContentType
import net.primal.android.premium.manage.content.repository.BroadcastRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel
class PremiumContentBackupViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val broadcastRepository: BroadcastRepository,
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
        observeEvents()
        fetchContentStats()
    }

    private fun fetchContentStats() {
        viewModelScope.launch {
            try {
                val stats = broadcastRepository.fetchContentStats(userId = activeAccountStore.activeUserId())
                setState {
                    copy(
                        allEventsCount = stats.values.sum(),
                        contentTypes = ContentGroup.entries.map { group ->
                            ContentType(
                                count = stats.filter { it.key in group.kinds }.values.sum(),
                                group = group,
                            )
                        },
                    )
                }
            } catch (error: WssException) {
                Timber.e(error)
            }
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    else -> {}
                }
            }
        }
    }
}
