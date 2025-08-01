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
import net.primal.android.user.domain.isNwcUrl
import net.primal.android.user.domain.parseNWCUrl
import net.primal.core.utils.asSha256Hash
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.NostrWalletKeypair
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletRepository
import timber.log.Timber

@HiltViewModel
class NwcQrCodeScannerViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
    private val walletAccountRepository: WalletAccountRepository,
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
            val nostrWalletConnect = runCatching { nwcUrl.parseNWCUrl() }
                .onFailure { Timber.w(it) }
                .getOrNull() ?: return@launch

            val walletId = nostrWalletConnect.keypair.privateKey.asSha256Hash()

            walletRepository.upsertNostrWallet(
                userId = activeAccountStore.activeUserId(),
                wallet = Wallet.NWC(
                    walletId = walletId,
                    userId = activeAccountStore.activeUserId(),
                    lightningAddress = nostrWalletConnect.lightningAddress,
                    balanceInBtc = null,
                    maxBalanceInBtc = null,
                    spamThresholdAmountInSats = 1L,
                    lastUpdatedAt = null,
                    relays = nostrWalletConnect.relays,
                    pubkey = nostrWalletConnect.pubkey,
                    keypair = NostrWalletKeypair(
                        privateKey = nostrWalletConnect.keypair.privateKey,
                        pubKey = nostrWalletConnect.keypair.pubkey,
                    ),
                ),
            )

            walletAccountRepository.setActiveWallet(
                userId = activeAccountStore.activeUserId(),
                walletId = walletId,
            )

            setEffect(NwcQrCodeScannerContract.SideEffect.NwcConnected)
        }
}
