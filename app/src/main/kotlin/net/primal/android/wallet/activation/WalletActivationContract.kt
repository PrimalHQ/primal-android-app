package net.primal.android.wallet.activation

import net.primal.android.core.errors.UiError
import net.primal.domain.account.Country
import net.primal.domain.account.State
import net.primal.domain.account.WalletActivationData
import net.primal.domain.account.WalletActivationStatus

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
        val uiError: UiError? = null,
        val activatedLightningAddress: String? = null,
    )

    sealed class UiEvent {
        data class ActivationDataChanged(val data: WalletActivationData) : UiEvent()
        data class OtpCodeChanged(val code: String) : UiEvent()
        data object ActivationRequest : UiEvent()
        data object Activate : UiEvent()
        data object ClearErrorMessage : UiEvent()
        data object DismissSnackbarError : UiEvent()
        data object RequestBackToDataInput : UiEvent()
    }

    data class ScreenCallbacks(
        val onDoneOrDismiss: () -> Unit,
    )
}
