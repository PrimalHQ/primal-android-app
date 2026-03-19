package net.primal.wallet.data.local

import net.primal.domain.wallet.WalletType
import net.primal.wallet.data.local.dao.WalletTransactionData

fun WalletTransactionData.isEligibleForZapEnrichment(): Boolean {
    return (txKind == TxKind.LIGHTNING || txKind == TxKind.SPARK) && invoice != null && walletType != WalletType.PRIMAL
}
