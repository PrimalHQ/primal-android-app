package net.primal.android.settings.wallet.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.math.BigDecimal
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.settings.wallet.connection.NwcNewWalletConnectionContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.repository.NwcWalletRepository
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import timber.log.Timber

@HiltViewModel
class NwcNewWalletConnectionViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val nwcWalletRepository: NwcWalletRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<NwcNewWalletConnectionContract.UiEvent>()
    fun setEvent(event: NwcNewWalletConnectionContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is NwcNewWalletConnectionContract.UiEvent.AppNameChanged -> setState {
                        copy(
                            appName = it.appName,
                            appNameInputError = false,
                        )
                    }

                    NwcNewWalletConnectionContract.UiEvent.CreateWalletConnection -> {
                        if (state.value.appName.isEmpty()) {
                            setState { copy(appNameInputError = true) }
                        } else {
                            createNewWalletConnection(
                                appName = state.value.appName,
                                dailyBudget = state.value.dailyBudget,
                            )
                        }
                    }

                    is NwcNewWalletConnectionContract.UiEvent.DailyBudgetChanged -> setState {
                        copy(
                            dailyBudget = it.dailyBudget,
                        )
                    }
                }
            }
        }
    }

    private fun createNewWalletConnection(appName: String, dailyBudget: String?) =
        viewModelScope.launch {
            try {
                setState { copy(appNameInputError = false, creatingSecret = true) }

                val dailyBudgetBtc = dailyBudget?.toLong()?.toBtc()

                val formattedDailyBudgetBtc = dailyBudgetBtc?.let {
                    BigDecimal(it.toString())
                        .stripTrailingZeros()
                        .toPlainString()
                }

                val response = nwcWalletRepository.createNewWalletConnection(
                    userId = activeAccountStore.activeUserId(),
                    appName = appName,
                    dailyBudget = formattedDailyBudgetBtc,
                )

                setState {
                    copy(
                        secret = response.nwcPubkey,
                        nwcConnectionUri = response.nwcConnectionUri,
                        creatingSecret = false,
                    )
                }
            } catch (error: WssException) {
                setState { copy(creatingSecret = false) }
                Timber.w(error)
            }
        }
}
