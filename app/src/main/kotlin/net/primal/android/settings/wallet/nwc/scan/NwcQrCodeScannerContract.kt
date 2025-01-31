package net.primal.android.settings.wallet.nwc.scan

import net.primal.android.scanner.domain.QrCodeResult

interface NwcQrCodeScannerContract {
    sealed class UiEvent {
        data class ProcessQrCodeResult(val result: QrCodeResult) : UiEvent()
        data class ProcessText(val text: String) : UiEvent()
    }

    sealed class SideEffect {
        data object NwcConnected : SideEffect()
    }
}
