package net.primal.domain.billing

import kotlin.coroutines.cancellation.CancellationException
import net.primal.domain.common.exception.NetworkException

interface BillingRepository {

    @Throws(
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun confirmInAppPurchase(
        userId: String,
        quoteId: String,
        purchaseToken: String,
    )

    @Throws(
        NetworkException::class,
        CancellationException::class,
    )
    suspend fun getInAppPurchaseMinSatsQuote(
        userId: String,
        region: String,
        productId: String,
        previousQuoteId: String?,
    ): InAppPurchaseSatsQuote
}
