package net.primal.android.settings.wallet.nwc.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.scanner.domain.QrCodeDataType
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.NWCParseException
import net.primal.android.user.domain.isNwcUrl
import net.primal.android.user.domain.parseNWCUrl
import net.primal.android.user.repository.UserRepository
import net.primal.core.utils.coroutines.DispatcherProvider
import timber.log.Timber

@HiltViewModel
class NwcQrCodeScannerViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val userRepository: UserRepository,
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
            try {
                val nostrWalletConnect = nwcUrl.parseNWCUrl()
                withContext(dispatcherProvider.io()) {
                    userRepository.connectNostrWallet(
                        userId = activeAccountStore.activeUserId(),
                        nostrWalletConnect = nostrWalletConnect,
                    )
                }
                setEffect(NwcQrCodeScannerContract.SideEffect.NwcConnected)
            } catch (error: NWCParseException) {
                Timber.w(error)
            }
        }
}
