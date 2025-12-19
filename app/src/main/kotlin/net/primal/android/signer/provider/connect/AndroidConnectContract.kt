package net.primal.android.signer.provider.connect

import android.graphics.drawable.Drawable
import net.primal.android.core.errors.UiError
import net.primal.android.drawer.multiaccount.model.UserAccountUi
import net.primal.domain.account.model.TrustLevel

interface AndroidConnectContract {

    data class UiState(
        val appPackageName: String,
        val appName: String,
        val appIcon: Drawable? = null,
        val accounts: List<UserAccountUi> = emptyList(),
        val connecting: Boolean = false,
        val error: UiError? = null,
    )

    sealed class UiEvent {
        data class ConnectUser(val userId: String, val trustLevel: TrustLevel) : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data class ConnectionSuccess(val userId: String) : SideEffect()
        data class ConnectionFailure(val error: Throwable) : SideEffect()
    }
}
