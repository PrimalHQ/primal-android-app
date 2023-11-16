package net.primal.android.user.accounts.active

import net.primal.android.user.domain.UserAccount

sealed class ActiveUserAccountState {

    data object NoUserAccount : ActiveUserAccountState()

    data class ActiveUserAccount(val data: UserAccount) : ActiveUserAccountState()
}
