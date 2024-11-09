package net.primal.android.premium.manage.nameChange

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.premium.manage.nameChange.PremiumChangePrimalNameContract.SideEffect
import net.primal.android.premium.manage.nameChange.PremiumChangePrimalNameContract.UiEvent
import net.primal.android.premium.manage.nameChange.PremiumChangePrimalNameContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class PremiumChangePrimalNameViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
        observeActiveAccount()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.SetPrimalName -> setState { copy(primalName = it.primalName) }
                    is UiEvent.SetStage -> setState { copy(stage = it.stage) }
                    UiEvent.ConfirmPrimalNameChange -> confirmPrimalNameChange()
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun confirmPrimalNameChange() =
        viewModelScope.launch {
            val primalName = state.value.primalName ?: throw IllegalStateException("primal name cannot be null")
            setState { copy(changingName = true) }
            delay(1.seconds)
            // TODO: actually call api here and change primal name
            /* on Error
            setState {
                copy(
                    changingName = false,
                    error = PremiumChangePrimalNameContract.NameChangeError.GenericError,
                )
            }
             */
            setState { copy(changingName = false) }
            setEffect(SideEffect.PrimalNameChanged)
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        primalName = it.premiumMembership?.premiumName,
                        profileAvatarCdnImage = it.avatarCdnImage,
                        profileDisplayName = it.authorDisplayName,
                    )
                }
            }
        }
}
