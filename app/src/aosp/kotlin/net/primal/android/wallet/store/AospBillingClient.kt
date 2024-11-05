package net.primal.android.wallet.store

import android.app.Activity
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import net.primal.android.wallet.store.domain.InAppProduct
import net.primal.android.wallet.store.domain.SatsPurchase
import net.primal.android.wallet.store.domain.SatsPurchaseQuote
import net.primal.android.wallet.store.domain.SubscriptionProduct
import net.primal.android.wallet.store.domain.SubscriptionPurchase

@Singleton
class AospBillingClient : PrimalBillingClient {

    override val satsPurchases: SharedFlow<SatsPurchase> = MutableSharedFlow()
    override val subscriptionPurchases: SharedFlow<SubscriptionPurchase> = MutableSharedFlow()

    override val minSatsInAppProduct: InAppProduct? = null
    override val subscriptionProducts: List<SubscriptionProduct> = emptyList()

    override suspend fun launchMinSatsBillingFlow(quote: SatsPurchaseQuote, activity: Activity) = Unit
    override suspend fun launchSubscriptionBillingFlow(subscriptionProduct: SubscriptionProduct, activity: Activity) =
        Unit

    override suspend fun refreshMinSatsInAppProduct() = Unit
    override suspend fun refreshSubscriptionProducts() = Unit
}
