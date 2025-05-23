package net.primal.android.wallet.repository

import net.primal.android.wallet.db.WalletTransactionData
import net.primal.domain.profile.ProfileData

data class TransactionProfileData(
    val transaction: WalletTransactionData,
    val otherProfileData: ProfileData?,
)
