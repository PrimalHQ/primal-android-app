package net.primal.android.premium.legend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.legend.PremiumBecomeLegendContract.Companion.LEGEND_THRESHOLD_IN_USD
import net.primal.android.premium.legend.PremiumBecomeLegendContract.UiEvent
import net.primal.android.premium.legend.PremiumBecomeLegendContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.repository.WalletRepository
import timber.log.Timber

@HiltViewModel
class PremiumBecomeLegendViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeActiveAccount()
        fetchExchangeRate()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.ShowAmountEditor -> setState {
                        copy(stage = PremiumBecomeLegendContract.BecomeLegendStage.PickAmount)
                    }

                    UiEvent.GoBackToIntro -> setState {
                        copy(stage = PremiumBecomeLegendContract.BecomeLegendStage.Intro)
                    }

                    UiEvent.ShowPaymentInstructions -> setState {
                        copy(stage = PremiumBecomeLegendContract.BecomeLegendStage.Payment)
                    }

                    UiEvent.ShowSuccess -> setState {
                        copy(stage = PremiumBecomeLegendContract.BecomeLegendStage.Success)
                    }
                }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(
                        displayName = it.authorDisplayName,
                        avatarCdnImage = it.avatarCdnImage,
                        profileNostrAddress = it.internetIdentifier,
                        profileLightningAddress = it.lightningAddress,
                        membership = it.premiumMembership,
                    )
                }
            }
        }

    private fun fetchExchangeRate() {
        viewModelScope.launch {
            try {
                val btcRate = walletRepository.getExchangeRate(
                    userId = activeAccountStore.activeUserId(),
                )
                setState {
                    copy(
                        minLegendThresholdInBtc = (LEGEND_THRESHOLD_IN_USD / btcRate).toBigDecimal(),
                        exchangeBtcUsdRate = btcRate,
                    )
                }
            } catch (error: WssException) {
                Timber.e(error)
            }
        }
    }
}
