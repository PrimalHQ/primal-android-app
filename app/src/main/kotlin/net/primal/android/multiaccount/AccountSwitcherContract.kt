package net.primal.android.multiaccount

import net.primal.android.multiaccount.model.UserAccountUi

interface AccountSwitcherContract {

    data class UiState(
        val userAccounts: List<UserAccountUi> = emptyList(),
        val activeAccount: UserAccountUi? = null,
    )

    sealed class UiEvent {
        data class SwitchAccount(val userId: String) : UiEvent()
    }

    sealed class SideEffect {
        data object AccountSwitched : SideEffect()
    }
}
