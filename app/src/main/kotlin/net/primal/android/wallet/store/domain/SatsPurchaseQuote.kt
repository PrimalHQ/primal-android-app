package net.primal.android.wallet.store.domain

data class SatsPurchaseQuote(
    val quoteId: String,
    val amountInBtc: String,
    val purchaseSymbol: String,
    val purchaseCurrency: String,
    val purchaseAmount: Long,
)
