package net.primal.android.user.active

import net.primal.android.user.domain.UserAccount

sealed class ActiveUserAccountState {

    object NoUserAccount : ActiveUserAccountState()

    data class ActiveUserAccount(
        val data: UserAccount
    ): ActiveUserAccountState()

}
