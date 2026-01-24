package net.primal.wallet.data.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.map
import net.primal.domain.rates.fees.OnChainTransactionFeeTier
import net.primal.domain.rates.fees.TransactionFeeRepository
import net.primal.domain.wallet.exception.WalletException
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.repository.mappers.local.toDomain
import net.primal.wallet.data.service.factory.WalletServiceFactory

internal class TransactionFeeRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val walletServiceFactory: WalletServiceFactory,
    private val walletDatabase: WalletDatabase,
) : TransactionFeeRepository {

    override suspend fun fetchMiningFees(
        userId: String,
        walletId: String,
        onChainAddress: String,
        amountInBtc: String,
    ): Result<List<OnChainTransactionFeeTier>> =
        withContext(dispatcherProvider.io()) {
            val wallet = walletDatabase.wallet().findWallet(walletId = walletId)
                ?: return@withContext Result.failure(WalletException.WalletNotFound())

            walletServiceFactory.getServiceForWallet(wallet.toDomain())
                .fetchMiningFees(
                    wallet = wallet.toDomain(),
                    onChainAddress = onChainAddress,
                    amountInBtc = amountInBtc,
                )
        }

    override suspend fun fetchDefaultMiningFee(
        userId: String,
        walletId: String,
        onChainAddress: String,
        amountInBtc: String,
    ): Result<OnChainTransactionFeeTier?> =
        fetchMiningFees(
            userId = userId,
            walletId = walletId,
            onChainAddress = onChainAddress,
            amountInBtc = amountInBtc,
        ).map { tiers ->
            tiers.lastOrNull {
                it.tierId.contains("standard", ignoreCase = true)
            } ?: tiers.lastOrNull {
                it.tierId.contains("fast", ignoreCase = true)
            } ?: tiers.lastOrNull()
        }
}
