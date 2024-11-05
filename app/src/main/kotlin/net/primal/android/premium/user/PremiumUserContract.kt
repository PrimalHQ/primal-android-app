package net.primal.android.premium.user

import net.primal.android.user.domain.UserAccount

interface PremiumUserContract {

    data class UiState(
        val userAccount: UserAccount = UserAccount.EMPTY,
    )
}
