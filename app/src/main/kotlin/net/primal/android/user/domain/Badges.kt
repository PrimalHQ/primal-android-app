package net.primal.android.user.domain

import kotlinx.serialization.Serializable

@Serializable
data class Badges(
    val unreadNotificationsCount: Int = 0,
    val unreadMessagesCount: Int = 0,
    val walletBalanceInBtc: String? = null,
)
