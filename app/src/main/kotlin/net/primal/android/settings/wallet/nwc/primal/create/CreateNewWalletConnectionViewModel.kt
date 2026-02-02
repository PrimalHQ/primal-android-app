package net.primal.android.settings.wallet.nwc.primal.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.settings.wallet.nwc.primal.create.CreateNewWalletConnectionContract.UiEvent
import net.primal.android.settings.wallet.nwc.primal.create.CreateNewWalletConnectionContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.connections.primal.PrimalWalletNwcRepository
import net.primal.domain.nostr.cryptography.SignatureException

@HiltViewModel
class CreateNewWalletConnectionViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val primalWalletNwcRepository: PrimalWalletNwcRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.AppNameChanged -> setState {
                        copy(appName = it.appName)
                    }

                    UiEvent.CreateWalletConnection -> {
                        if (state.value.appName.isNotEmpty()) {
                            createNewWalletConnection(
                                appName = state.value.appName,
                                dailyBudget = state.value.dailyBudget,
                            )
                        }
                    }

                    is UiEvent.DailyBudgetChanged -> setState {
                        copy(dailyBudget = it.dailyBudget)
                    }
                }
            }
        }
    }

    private fun createNewWalletConnection(appName: String, dailyBudget: Long?) =
        viewModelScope.launch {
            try {
                setState { copy(creatingSecret = true) }

                val dailyBudgetBtc = dailyBudget?.toBtc()

                val formattedDailyBudgetBtc = dailyBudgetBtc?.let {
                    BigDecimal(it.toString())
                        .stripTrailingZeros()
                        .toPlainString()
                }

                val response = primalWalletNwcRepository.createNewWalletConnection(
                    userId = activeAccountStore.activeUserId(),
                    appName = appName,
                    dailyBudget = formattedDailyBudgetBtc,
                )

                setState {
                    copy(
                        nwcConnectionUri = response.nwcConnectionUri,
                        creatingSecret = false,
                    )
                }
            } catch (error: SignatureException) {
                setState { copy(creatingSecret = false) }
                Napier.w(throwable = error) { "Failed to create wallet connection due to signature error." }
            } catch (error: NetworkException) {
                setState { copy(creatingSecret = false) }
                Napier.w(throwable = error) { "Failed to create wallet connection due to network error." }
            }
        }
}
