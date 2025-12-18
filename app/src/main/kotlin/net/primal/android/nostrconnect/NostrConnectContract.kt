package net.primal.android.nostrconnect

import net.primal.android.core.compose.signer.model.SignerConnectTab
import net.primal.android.core.errors.UiError
import net.primal.android.drawer.multiaccount.model.UserAccountUi
import net.primal.domain.account.model.TrustLevel

interface NostrConnectContract {
    data class UiState(
        val appName: String?,
        val appDescription: String?,
        val appImageUrl: String?,
        val connectionUrl: String?,
        val accounts: List<UserAccountUi> = emptyList(),
        val selectedTab: SignerConnectTab = SignerConnectTab.Login,
        val selectedAccount: UserAccountUi? = null,
        val trustLevel: TrustLevel = TrustLevel.Medium,
        val connecting: Boolean = false,
        val error: UiError? = null,
    )

    sealed class UiEvent {
        data class ChangeTab(val tab: SignerConnectTab) : UiEvent()
        data class SelectAccount(val pubkey: String) : UiEvent()
        data class SelectTrustLevel(val level: TrustLevel) : UiEvent()
        data object ClickConnect : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object ConnectionSuccess : SideEffect()
    }
}
