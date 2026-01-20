package net.primal.wallet.data.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.connections.PrimalWalletNwcRepository
import net.primal.domain.connections.model.NwcConnection
import net.primal.domain.connections.model.NwcConnectionInfo
import net.primal.wallet.data.remote.api.PrimalWalletNwcApi

class PrimalWalletNwcRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val primalWalletNwcApi: PrimalWalletNwcApi,
) : PrimalWalletNwcRepository {

    override suspend fun getConnections(userId: String): List<NwcConnectionInfo> {
        val response = withContext(dispatcherProvider.io()) {
            primalWalletNwcApi.getConnections(userId)
        }
        return response.map {
            NwcConnectionInfo(
                appName = it.appName,
                dailyBudgetInBtc = it.dailyBudget,
                nwcPubkey = it.nwcPubkey,
            )
        }
    }

    override suspend fun createNewWalletConnection(
        userId: String,
        appName: String,
        dailyBudget: String?,
    ): NwcConnection {
        val response = withContext(dispatcherProvider.io()) {
            primalWalletNwcApi.createNewWalletConnection(
                userId = userId,
                appName = appName,
                dailyBudgetBtc = dailyBudget,
            )
        }
        return NwcConnection(
            nwcPubkey = response.nwcPubkey,
            nwcConnectionUri = response.nwcConnectionUri,
        )
    }

    override suspend fun revokeConnection(userId: String, nwcPubkey: String) {
        return withContext(dispatcherProvider.io()) {
            primalWalletNwcApi.revokeConnection(userId = userId, nwcPubkey = nwcPubkey)
        }
    }
}
