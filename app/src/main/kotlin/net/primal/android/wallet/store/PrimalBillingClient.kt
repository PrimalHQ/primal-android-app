package net.primal.android.wallet.store

import android.app.Activity
import kotlinx.coroutines.flow.SharedFlow
import net.primal.android.wallet.store.domain.InAppProduct
import net.primal.android.wallet.store.domain.SatsPurchase
import net.primal.android.wallet.store.domain.SatsPurchaseQuote

interface PrimalBillingClient {

    val purchases: SharedFlow<SatsPurchase>

    val minSatsInAppProduct: InAppProduct?

    suspend fun launchMinSatsBillingFlow(quote: SatsPurchaseQuote, activity: Activity)
}
