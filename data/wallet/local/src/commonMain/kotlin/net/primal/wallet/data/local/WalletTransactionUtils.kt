package net.primal.wallet.data.local

import net.primal.domain.wallet.WalletType
import net.primal.wallet.data.local.dao.WalletTransactionData

// 2026-03-01T00:00:00Z — Spark transactions before this date are not eligible
private const val SPARK_ENRICHMENT_CUTOFF_EPOCH = 1772438400L

fun WalletTransactionData.isEligibleForZapEnrichment(): Boolean {
    if (invoice == null || walletType == WalletType.PRIMAL) return false
    return when (txKind) {
        TxKind.LIGHTNING -> true
        TxKind.SPARK -> createdAt >= SPARK_ENRICHMENT_CUTOFF_EPOCH
        else -> false
    }
}
