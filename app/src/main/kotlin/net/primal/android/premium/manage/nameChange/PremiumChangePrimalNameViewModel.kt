package net.primal.android.premium.manage.nameChange

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
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.premium.manage.nameChange.PremiumChangePrimalNameContract.ChangePrimalNameStage
import net.primal.android.premium.manage.nameChange.PremiumChangePrimalNameContract.SideEffect
import net.primal.android.premium.manage.nameChange.PremiumChangePrimalNameContract.UiEvent
import net.primal.android.premium.manage.nameChange.PremiumChangePrimalNameContract.UiState
import net.primal.android.premium.repository.PremiumRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel
class PremiumChangePrimalNameViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val premiumRepository: PremiumRepository,
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
            val primalName = state.value.primalName ?: error("primal name cannot be null")
            setState { copy(changingName = true) }

            try {
                val result = premiumRepository.changePrimalName(
                    userId = activeAccountStore.activeUserId(),
                    name = primalName,
                )

                if (!result) {
                    setState {
                        copy(
                            error = PremiumChangePrimalNameContract.NameChangeError.PrimalNameTaken,
                            stage = ChangePrimalNameStage.PickNew,
                        )
                    }
                } else {
                    premiumRepository.fetchMembershipStatus(userId = activeAccountStore.activeUserId())
                    setEffect(SideEffect.PrimalNameChanged)
                }
            } catch (error: MissingPrivateKeyException) {
                Timber.w(error)
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = PremiumChangePrimalNameContract.NameChangeError.GenericError) }
            } finally {
                setState { copy(changingName = false) }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        primalName = it.premiumMembership?.premiumName,
                        profileAvatarCdnImage = it.avatarCdnImage,
                        profileLegendaryCustomization = it.primalLegendProfile?.asLegendaryCustomization(),
                    )
                }
            }
        }
}
