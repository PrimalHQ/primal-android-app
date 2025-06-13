package net.primal.wallet.data.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.rates.fees.OnChainTransactionFeeTier
import net.primal.domain.rates.fees.TransactionFeeRepository
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.repository.mappers.remote.asOnChainTxFeeTierDO

class TransactionFeeRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val primalWalletApi: PrimalWalletApi,
) : TransactionFeeRepository {

    override suspend fun fetchMiningFees(
        userId: String,
        onChainAddress: String,
        amountInBtc: String,
    ): List<OnChainTransactionFeeTier> {
        return withContext(dispatcherProvider.io()) {
            primalWalletApi.getMiningFees(
                userId = userId,
                onChainAddress = onChainAddress,
                amountInBtc = amountInBtc,
            ).map {
                it.asOnChainTxFeeTierDO()
            }
        }
    }

    override suspend fun fetchDefaultMiningFee(
        userId: String,
        onChainAddress: String,
        amountInBtc: String,
    ): OnChainTransactionFeeTier? =
        withContext(dispatcherProvider.io()) {
            val tiers = fetchMiningFees(
                userId = userId,
                onChainAddress = onChainAddress,
                amountInBtc = amountInBtc,
            )

            tiers.lastOrNull {
                it.tierId.contains("standard", ignoreCase = true)
            } ?: tiers.lastOrNull {
                it.tierId.contains("fast", ignoreCase = true)
            } ?: tiers.lastOrNull()
        }
}
