package net.primal.android.signer.provider.connect

import net.primal.domain.account.model.TrustLevel

interface AndroidConnectContract {

    data class UiState(
        val appPackageName: String,
    )

    sealed class UiEvent {
        data class ConnectUser(val userId: String, val trustLevel: TrustLevel) : UiEvent()
    }
    sealed class SideEffect {
        data class ConnectionSuccess(val userId: String) : SideEffect()
        data class ConnectionFailure(val error: Throwable) : SideEffect()
    }
}
