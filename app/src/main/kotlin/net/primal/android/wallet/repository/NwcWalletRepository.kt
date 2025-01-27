package net.primal.android.wallet.repository

import javax.inject.Inject
import net.primal.android.wallet.api.NwcPrimalWalletApi
import net.primal.android.wallet.api.model.PrimalNwcConnectionInfo

class NwcWalletRepository @Inject constructor(
    private val nwcPrimalWalletApi: NwcPrimalWalletApi,
) {
    suspend fun getConnections(userId: String): List<PrimalNwcConnectionInfo> {
        return nwcPrimalWalletApi.getConnections(userId)
    }

    suspend fun revokeConnection(userId: String, nwcPubkey: String) {
        return nwcPrimalWalletApi.revokeConnection(userId = userId, nwcPubkey = nwcPubkey)
    }
}
