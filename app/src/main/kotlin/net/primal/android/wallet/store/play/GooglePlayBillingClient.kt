package net.primal.android.wallet.store.play

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GooglePlayBillingClient @Inject constructor(
    @ApplicationContext appContext: Context,
) : PurchasesUpdatedListener, BillingClientStateListener {

    private val billingClient by lazy {
        BillingClient.newBuilder(appContext)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }

    private var connected: Boolean = false

    init {
        initConnection()
    }

    private fun initConnection() {
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            connected = true
        }
    }

    override fun onBillingServiceDisconnected() {
        connected = false
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
    }
}
