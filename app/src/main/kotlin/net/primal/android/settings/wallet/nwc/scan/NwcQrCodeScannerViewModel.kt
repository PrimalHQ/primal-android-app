package net.primal.android.settings.wallet.nwc.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.scanner.domain.QrCodeDataType
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.parser.isNwcUrl
import net.primal.domain.usecase.ConnectNwcUseCase
import timber.log.Timber

@HiltViewModel
class NwcQrCodeScannerViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val connectNwcUseCase: ConnectNwcUseCase,
) : ViewModel() {

    private val events = MutableSharedFlow<NwcQrCodeScannerContract.UiEvent>()
    fun setEvent(event: NwcQrCodeScannerContract.UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<NwcQrCodeScannerContract.SideEffect>()
    private fun setEffect(effect: NwcQrCodeScannerContract.SideEffect) = viewModelScope.launch { _effects.send(effect) }
    val effects = _effects.receiveAsFlow()

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is NwcQrCodeScannerContract.UiEvent.ProcessQrCodeResult -> processQrCodeResult(it.result)
                    is NwcQrCodeScannerContract.UiEvent.ProcessText -> processText(it.text)
                }
            }
        }
    }

    private fun processQrCodeResult(result: QrCodeResult) =
        viewModelScope.launch {
            when (result.type) {
                QrCodeDataType.NWC_URL -> connectWallet(nwcUrl = result.value)
                else -> Unit
            }
        }

    private fun processText(text: String) {
        viewModelScope.launch {
            if (text.isNwcUrl()) {
                connectWallet(nwcUrl = text)
            }
        }
    }

    private fun connectWallet(nwcUrl: String) =
        viewModelScope.launch {
            connectNwcUseCase.invoke(userId = activeAccountStore.activeUserId(), nwcUrl = nwcUrl)
                .onFailure { Timber.w(it) }
                .onSuccess { setEffect(NwcQrCodeScannerContract.SideEffect.NwcConnected) }
        }
}
