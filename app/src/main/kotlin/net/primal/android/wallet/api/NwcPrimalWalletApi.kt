package net.primal.android.wallet.api

import net.primal.android.wallet.api.model.NewNwcConnectionInfo
import net.primal.android.wallet.api.model.PrimalNwcConnectionInfo

interface NwcPrimalWalletApi {

    suspend fun getConnections(userId: String): List<PrimalNwcConnectionInfo>

    suspend fun revokeConnection(userId: String, nwcPubkey: String)

    suspend fun createNewWalletConnection(
        userId: String,
        appName: String,
        dailyBudgetBtc: String?,
    ): NewNwcConnectionInfo
}
