package net.primal.android.settings.wallet.nwc.primal.link

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.settings.wallet.domain.PrimalWalletNwc
import net.primal.android.settings.wallet.nwc.primal.PrimalNwcDefaults.DEFAULT_APP_NAME
import net.primal.android.settings.wallet.nwc.primal.link.LinkPrimalWalletContract.SideEffect
import net.primal.android.settings.wallet.nwc.primal.link.LinkPrimalWalletContract.UiEvent
import net.primal.android.settings.wallet.nwc.primal.link.LinkPrimalWalletContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.repository.NwcWalletRepository
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel(assistedFactory = LinkPrimalWalletViewModel.Factory::class)
class LinkPrimalWalletViewModel @AssistedInject constructor(
    @Assisted private val nwcRequest: PrimalWalletNwc,
    private val activeAccountStore: ActiveAccountStore,
    private val nwcWalletRepository: NwcWalletRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(nwcRequest: PrimalWalletNwc): LinkPrimalWalletViewModel
    }

    private val _state = MutableStateFlow(
        UiState(
            callback = nwcRequest.callback ?: "",
            appName = nwcRequest.appName ?: DEFAULT_APP_NAME,
            appIcon = nwcRequest.appIcon,
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
                    appName = state.value.appName ?: DEFAULT_APP_NAME,
                    dailyBudget = formattedDailyBudgetBtc,
                )

                setState {
                    copy(
                        nwcConnectionUri = response.nwcConnectionUri,
                        creatingSecret = false,
                    )
                }

                setEffect(
                    SideEffect.UriReceived(
                        callbackUri = "${_state.value.callback}?value=${response.nwcConnectionUri.urlEncode()}",
                    ),
                )
            } catch (error: WssException) {
                setState { copy(creatingSecret = false) }
                Timber.w(error)
            }
        }

    private fun String.urlEncode(): String =
        runCatching {
            URLEncoder.encode(this, StandardCharsets.UTF_8.toString())
        }.getOrNull() ?: this
}
