package net.primal.android.settings.wallet.nwc.primal.create

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
import net.primal.android.nostr.notary.exceptions.SignException
import net.primal.android.settings.wallet.nwc.primal.create.CreateNewWalletConnectionContract.UiEvent
import net.primal.android.settings.wallet.nwc.primal.create.CreateNewWalletConnectionContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.repository.NwcWalletRepository
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.core.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel
class CreateNewWalletConnectionViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val nwcWalletRepository: NwcWalletRepository,
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

                val response = nwcWalletRepository.createNewWalletConnection(
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
            } catch (error: SignException) {
                setState { copy(creatingSecret = false) }
                Timber.w(error)
            } catch (error: WssException) {
                setState { copy(creatingSecret = false) }
                Timber.w(error)
            }
        }
}
