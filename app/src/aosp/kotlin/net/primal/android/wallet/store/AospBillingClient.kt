package net.primal.android.wallet.store

import android.app.Activity
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import net.primal.android.wallet.store.domain.InAppProduct
import net.primal.android.wallet.store.domain.SatsPurchase
import net.primal.android.wallet.store.domain.SatsPurchaseQuote

@Singleton
class AospBillingClient : PrimalBillingClient {

    override val purchases: SharedFlow<SatsPurchase> = MutableSharedFlow()

    override val minSatsInAppProduct: InAppProduct? = null

    override suspend fun launchMinSatsBillingFlow(quote: SatsPurchaseQuote, activity: Activity) = Unit

    override suspend fun refreshMinSatsInAppProduct() = Unit
}
