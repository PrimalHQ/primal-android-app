package net.primal.wallet.data.remote.api

import net.primal.wallet.data.remote.model.InAppPurchaseQuoteResponse
import net.primal.wallet.data.remote.model.ParsedLnInvoiceResponse
import net.primal.wallet.data.remote.model.ParsedLnUrlResponse
import net.primal.wallet.data.remote.model.WalletStatusResponse

interface PrimalWalletApi {

    suspend fun getWalletStatus(userId: String): WalletStatusResponse

    suspend fun getInAppPurchaseQuote(
        userId: String,
        productId: String,
        region: String,
        previousQuoteId: String? = null,
    ): InAppPurchaseQuoteResponse

    suspend fun confirmInAppPurchase(
        userId: String,
        quoteId: String,
        purchaseToken: String,
    )

    suspend fun parseLnUrl(userId: String, lnurl: String): ParsedLnUrlResponse

    suspend fun parseLnInvoice(userId: String, lnbc: String): ParsedLnInvoiceResponse

    suspend fun getExchangeRate(userId: String): Double

    suspend fun registerSparkWallet(userId: String, sparkWalletId: String)

    suspend fun unregisterSparkWallet(userId: String, sparkWalletId: String)
}
