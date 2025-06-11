package net.primal.android.wallet.repository

import net.primal.domain.profile.ProfileData
import net.primal.wallet.data.local.dao.WalletTransactionData

data class TransactionProfileData(
    val transaction: WalletTransactionData,
    val otherProfileData: ProfileData?,
)
