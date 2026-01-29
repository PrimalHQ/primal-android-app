package net.primal.wallet.data.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.connections.primal.PrimalWalletNwcRepository
import net.primal.domain.connections.primal.model.PrimalNwcConnection
import net.primal.domain.connections.primal.model.PrimalNwcConnectionInfo
import net.primal.wallet.data.remote.api.PrimalWalletNwcApi

internal class PrimalWalletNwcRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val primalWalletNwcApi: PrimalWalletNwcApi,
) : PrimalWalletNwcRepository {

    override suspend fun getConnections(userId: String): List<PrimalNwcConnectionInfo> {
        val response = withContext(dispatcherProvider.io()) {
            primalWalletNwcApi.getConnections(userId)
        }
        return response.map {
            PrimalNwcConnectionInfo(
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
    ): PrimalNwcConnection {
        val response = withContext(dispatcherProvider.io()) {
            primalWalletNwcApi.createNewWalletConnection(
                userId = userId,
                appName = appName,
                dailyBudgetBtc = dailyBudget,
            )
        }
        return PrimalNwcConnection(
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
