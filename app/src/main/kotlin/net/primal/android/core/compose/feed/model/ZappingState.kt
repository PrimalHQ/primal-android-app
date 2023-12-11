package net.primal.android.core.compose.feed.model

import net.primal.android.user.domain.WalletPreference

data class ZappingState(
    val walletConnected: Boolean = false,
    val walletPreference: WalletPreference = WalletPreference.Undefined,
    val walletBalanceInBtc: String? = null,
    val defaultZapAmount: ULong = 42.toULong(),
    val zapOptions: List<ULong> = emptyList(),
)
