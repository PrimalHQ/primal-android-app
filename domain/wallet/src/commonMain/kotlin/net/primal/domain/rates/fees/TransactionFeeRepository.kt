package net.primal.domain.rates.fees

import net.primal.core.utils.Result

interface TransactionFeeRepository {

    suspend fun fetchMiningFees(
        userId: String,
        walletId: String,
        onChainAddress: String,
        amountInBtc: String,
    ): Result<List<OnChainTransactionFeeTier>>

    suspend fun fetchDefaultMiningFee(
        userId: String,
        walletId: String,
        onChainAddress: String,
        amountInBtc: String,
    ): Result<OnChainTransactionFeeTier?>
}
