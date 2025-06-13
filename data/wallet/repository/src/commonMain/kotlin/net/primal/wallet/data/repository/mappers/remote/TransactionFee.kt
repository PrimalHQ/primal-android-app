package net.primal.wallet.data.repository.mappers.remote

import net.primal.domain.rates.fees.OnChainTransactionFeeTier as OnChainTransactionFeeTierDO
import net.primal.wallet.data.remote.model.MiningFeeTier

internal fun MiningFeeTier.asOnChainTxFeeTierDO(): OnChainTransactionFeeTierDO {
    return OnChainTransactionFeeTierDO(
        tierId = this.id,
        label = this.label,
        confirmationEstimationInMin = this.estimatedDeliveryDurationInMin,
        txFeeInBtc = this.estimatedFee.amount,
        minAmountInBtc = this.minimumAmount?.amount,
    )
}
