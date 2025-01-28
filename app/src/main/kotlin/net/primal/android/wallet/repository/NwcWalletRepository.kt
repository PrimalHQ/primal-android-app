package net.primal.android.wallet.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.wallet.api.NwcPrimalWalletApi
import net.primal.android.wallet.api.model.PrimalNwcConnectionInfo

class NwcWalletRepository @Inject constructor(
    private val nwcPrimalWalletApi: NwcPrimalWalletApi,
    private val dispatcherProvider: CoroutineDispatcherProvider,
) {

    suspend fun getConnections(userId: String): List<PrimalNwcConnectionInfo> {
        return withContext(dispatcherProvider.io()) {
            nwcPrimalWalletApi.getConnections(userId)
        }
    }

    suspend fun revokeConnection(userId: String, nwcPubkey: String) {
        return withContext(dispatcherProvider.io()) {
            nwcPrimalWalletApi.revokeConnection(userId = userId, nwcPubkey = nwcPubkey)
        }
    }
}
