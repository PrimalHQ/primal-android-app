package net.primal.wallet.data.remote.api

import net.primal.wallet.data.remote.model.NwcConnectionCreatedResponse
import net.primal.wallet.data.remote.model.PrimalNwcConnectionInfo

interface PrimalWalletNwcApi {

    suspend fun getConnections(userId: String): List<PrimalNwcConnectionInfo>

    suspend fun revokeConnection(userId: String, nwcPubkey: String)

    suspend fun createNewWalletConnection(
        userId: String,
        appName: String,
        dailyBudgetBtc: String?,
    ): NwcConnectionCreatedResponse
}
