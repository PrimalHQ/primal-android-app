package net.primal.wallet.data.repository

import kotlinx.coroutines.withContext
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.billing.BillingRepository
import net.primal.domain.billing.InAppPurchaseSatsQuote
import net.primal.wallet.data.remote.api.PrimalWalletApi

internal class BillingRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val primalWalletApi: PrimalWalletApi,
) : BillingRepository {

    override suspend fun getInAppPurchaseMinSatsQuote(
        userId: String,
        region: String,
        productId: String,
        previousQuoteId: String?,
    ): InAppPurchaseSatsQuote {
        return withContext(dispatcherProvider.io()) {
            val response = primalWalletApi.getInAppPurchaseQuote(
                userId = userId,
                productId = productId,
                region = region,
                previousQuoteId = previousQuoteId,
            )
            InAppPurchaseSatsQuote(
                quoteId = response.quoteId,
                amountBtc = response.amountBtc,
            )
        }
    }

    override suspend fun confirmInAppPurchase(
        userId: String,
        quoteId: String,
        purchaseToken: String,
    ) {
        return withContext(dispatcherProvider.io()) {
            primalWalletApi.confirmInAppPurchase(
                userId = userId,
                quoteId = quoteId,
                purchaseToken = purchaseToken,
            )
        }
    }
}
