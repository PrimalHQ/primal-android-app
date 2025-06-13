package net.primal.android.wallet.repository

import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.wallet.data.remote.api.PrimalWalletNwcApi
import net.primal.wallet.data.remote.model.NwcConnectionCreatedResponse
import net.primal.wallet.data.remote.model.PrimalNwcConnectionInfo

class NwcWalletRepository @Inject constructor(
    private val primalWalletNwcApi: PrimalWalletNwcApi,
    private val dispatcherProvider: DispatcherProvider,
) {

    suspend fun getConnections(userId: String): List<PrimalNwcConnectionInfo> {
        return withContext(dispatcherProvider.io()) {
            primalWalletNwcApi.getConnections(userId)
        }
    }

    suspend fun createNewWalletConnection(
        userId: String,
        appName: String,
        dailyBudget: String?,
    ): NwcConnectionCreatedResponse {
        return withContext(dispatcherProvider.io()) {
            primalWalletNwcApi.createNewWalletConnection(
                userId = userId,
                appName = appName,
                dailyBudgetBtc = dailyBudget,
            )
        }
    }

    suspend fun revokeConnection(userId: String, nwcPubkey: String) {
        return withContext(dispatcherProvider.io()) {
            primalWalletNwcApi.revokeConnection(userId = userId, nwcPubkey = nwcPubkey)
        }
    }
}
