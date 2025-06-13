package net.primal.domain.rates.fees

import kotlin.coroutines.cancellation.CancellationException
import net.primal.domain.common.exception.NetworkException

interface TransactionFeeRepository {

    @Throws(
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun fetchMiningFees(
        userId: String,
        onChainAddress: String,
        amountInBtc: String,
    ): List<OnChainTransactionFeeTier>

    @Throws(
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun fetchDefaultMiningFee(
        userId: String,
        onChainAddress: String,
        amountInBtc: String,
    ): OnChainTransactionFeeTier?
}
