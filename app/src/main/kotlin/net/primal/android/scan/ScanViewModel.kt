package net.primal.android.scan

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
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.crypto.urlToLnUrlHrp
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.scan.ScanContract.SideEffect
import net.primal.android.scan.ScanContract.UiEvent
import net.primal.android.scan.ScanContract.UiState
import net.primal.android.scan.analysis.QrCodeDataType
import net.primal.android.scan.analysis.QrCodeResult
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.api.parseAsLNUrlOrNull
import net.primal.android.wallet.repository.WalletRepository
import timber.log.Timber

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val dispatchers: CoroutineDispatcherProvider,
    private val walletRepository: WalletRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        subscribeToEvents()
    }

    private fun subscribeToEvents() {
        viewModelScope.launch {
            events.collect {
                processEvent(it)
            }
        }
    }

    private fun processEvent(event: UiEvent) {
        when (event) {
            is UiEvent.ProcessScannedData -> processScannedData(result = event.result)
        }
    }

    private fun processScannedData(result: QrCodeResult) =
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            val qrCodeValue = result.value
            Timber.i("Processing QR CODE: $qrCodeValue")

            try {
                when (result.type) {
                    QrCodeDataType.LNBC -> {
                        withContext(dispatchers.io()) {
                            walletRepository.parseLnInvoice(userId = userId, lnbc = qrCodeValue)
                        }
                        setEffect(SideEffect.ScanningCompleted)
                    }

                    QrCodeDataType.LNURL -> {
                        withContext(dispatchers.io()) {
                            walletRepository.parseLnUrl(userId = userId, lnurl = qrCodeValue)
                        }
                        setEffect(SideEffect.ScanningCompleted)
                    }

                    QrCodeDataType.LUD16 -> {
                        val lud16Value = qrCodeValue.split(":").last()
                        val lnurl = lud16Value.parseAsLNUrlOrNull()?.urlToLnUrlHrp()
                        if (lnurl != null) {
                            withContext(dispatchers.io()) {
                                walletRepository.parseLnUrl(userId = userId, lnurl = lnurl)
                            }
                        }
                        setEffect(SideEffect.ScanningCompleted)
                    }

                    else -> Unit
                }
            } catch (error: WssException) {
                Timber.e(error)
            }
        }
}
