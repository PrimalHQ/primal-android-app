package net.primal.android.wallet.store

import android.app.Activity
import kotlinx.coroutines.flow.SharedFlow
import net.primal.android.wallet.store.domain.InAppProduct
import net.primal.android.wallet.store.domain.SatsPurchase
import net.primal.android.wallet.store.domain.SatsPurchaseQuote
import net.primal.android.wallet.store.domain.SubscriptionProduct
import net.primal.android.wallet.store.domain.SubscriptionPurchase

interface PrimalBillingClient {

    val satsPurchases: SharedFlow<SatsPurchase>

    val subscriptionPurchases: SharedFlow<SubscriptionPurchase>

    val minSatsInAppProduct: InAppProduct?

    val subscriptionProducts: List<SubscriptionProduct>

    suspend fun fetchBillingProducts()

    suspend fun launchMinSatsBillingFlow(quote: SatsPurchaseQuote, activity: Activity)

    suspend fun launchSubscriptionBillingFlow(subscriptionProduct: SubscriptionProduct, activity: Activity)

}
