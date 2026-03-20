package net.primal.wallet.data.local

import net.primal.domain.wallet.WalletType
import net.primal.wallet.data.local.dao.WalletTransactionData

const val ENRICHMENT_CUTOFF_EPOCH_SECONDS: Long = 1772323200 // 2026-03-01T00:00:00Z

fun WalletTransactionData.isEligibleForZapEnrichment(): Boolean {
    return (txKind == TxKind.LIGHTNING || txKind == TxKind.SPARK) &&
        invoice != null &&
        walletType != WalletType.PRIMAL &&
        (createdAt >= ENRICHMENT_CUTOFF_EPOCH_SECONDS || walletType == WalletType.NWC)
}
