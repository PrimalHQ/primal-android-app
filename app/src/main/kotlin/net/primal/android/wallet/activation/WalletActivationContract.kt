package net.primal.android.wallet.activation

interface WalletActivationContract {
    data class UiState(
        val status: WalletActivationStatus = WalletActivationStatus.PendingData,
        val data: WalletActivationData = WalletActivationData(),
        val working: Boolean = false,
        val error: Throwable? = null,
        val activatedLightningAddress: String? = null,
    )

    sealed class UiEvent {
        data class ActivationDataChanged(val data: WalletActivationData) : UiEvent()
        data class ActivationRequest(val data: WalletActivationData) : UiEvent()
        data class Activate(val code: String) : UiEvent()
        data object ClearErrorMessage : UiEvent()
        data object RequestBackToDataInput : UiEvent()
    }
}
