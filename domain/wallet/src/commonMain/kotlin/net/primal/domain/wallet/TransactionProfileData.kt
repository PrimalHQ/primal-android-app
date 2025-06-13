package net.primal.domain.wallet

import net.primal.domain.profile.ProfileData

data class TransactionWithProfile(
    val transaction: WalletTransaction,
    val otherProfileData: ProfileData?,
)
