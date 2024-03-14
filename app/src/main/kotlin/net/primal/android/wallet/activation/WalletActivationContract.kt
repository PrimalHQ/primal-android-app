package net.primal.android.wallet.activation

import net.primal.android.wallet.activation.domain.WalletActivationData
import net.primal.android.wallet.activation.domain.WalletActivationStatus
import net.primal.android.wallet.activation.regions.Country
import net.primal.android.wallet.activation.regions.State

interface WalletActivationContract {
    data class UiState(
        val status: WalletActivationStatus = WalletActivationStatus.PendingData,
        val allCountries: List<Country> = emptyList(),
        val availableStates: List<State> = emptyList(),
        val data: WalletActivationData = WalletActivationData(),
        val isDataValid: Boolean = false,
        val otpCode: String = "",
        val working: Boolean = false,
        val error: Throwable? = null,
        val activatedLightningAddress: String? = null,
    )

    sealed class UiEvent {
        data class ActivationDataChanged(val data: WalletActivationData) : UiEvent()
        data class OtpCodeChanged(val code: String) : UiEvent()
        data object ActivationRequest : UiEvent()
        data object Activate : UiEvent()
        data object ClearErrorMessage : UiEvent()
        data object RequestBackToDataInput : UiEvent()
    }
}
