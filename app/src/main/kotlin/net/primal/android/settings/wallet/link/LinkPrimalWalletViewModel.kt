package net.primal.android.settings.wallet.link

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.navigation.appIcon
import net.primal.android.navigation.appName
import net.primal.android.navigation.callback
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.settings.wallet.link.LinkPrimalWalletContract.SideEffect
import net.primal.android.settings.wallet.link.LinkPrimalWalletContract.UiEvent
import net.primal.android.settings.wallet.link.LinkPrimalWalletContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.repository.NwcWalletRepository
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import timber.log.Timber

@HiltViewModel
class LinkPrimalWalletViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val nwcWalletRepository: NwcWalletRepository,
) : ViewModel() {

    private val appName: String = savedStateHandle.appName ?: "External app"
    private val appIcon: String? = savedStateHandle.appIcon
    private val callback: String = savedStateHandle.callback

    private val _state = MutableStateFlow(
        UiState(
            appName = appName,
            appIcon = appIcon,
            callback = callback,
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.DailyBudgetChanged -> setState {
                        copy(dailyBudget = it.dailyBudget)
                    }

                    UiEvent.CreateWalletConnection -> createNewWalletConnection(dailyBudget = state.value.dailyBudget)
                }
            }
        }
    }

    private fun createNewWalletConnection(dailyBudget: Long?) =
        viewModelScope.launch {
            try {
                setState { copy(creatingSecret = true) }

                val dailyBudgetBtc = dailyBudget?.toBtc()

                val formattedDailyBudgetBtc = dailyBudgetBtc?.let {
                    BigDecimal(it.toString())
                        .stripTrailingZeros()
                        .toPlainString()
                }

                val response = nwcWalletRepository.createNewWalletConnection(
                    userId = activeAccountStore.activeUserId(),
                    appName = state.value.appName ?: "External app",
                    dailyBudget = formattedDailyBudgetBtc,
                )

                setState {
                    copy(
                        secret = response.nwcConnectionUri,
                        nwcConnectionUri = response.nwcConnectionUri,
                        creatingSecret = false,
                    )
                }

                setEffect(SideEffect.UriReceived(nwcConnectionUri = response.nwcConnectionUri))
            } catch (error: WssException) {
                setState { copy(creatingSecret = false) }
                Timber.w(error)
            }
        }
}
