package net.primal.android.wallet.store.play

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.queryProductDetails
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.wallet.store.PrimalBillingClient
import net.primal.android.wallet.store.domain.InAppProduct
import net.primal.android.wallet.store.domain.SatsPurchase
import net.primal.android.wallet.store.domain.SatsPurchaseQuote
import timber.log.Timber

class PlayBillingClient @Inject constructor(
    @ApplicationContext appContext: Context,
    dispatchers: CoroutineDispatcherProvider,
) : PrimalBillingClient, PurchasesUpdatedListener {

    private val scope = CoroutineScope(dispatchers.io())

    private val billingClient by lazy {
        BillingClient.newBuilder(appContext)
            .setListener(this)
            .enablePendingPurchases()
            .build()
    }

    private var connected: Boolean = false
    private val connectLock: Mutex = Mutex()

    private var purchaseQuote: SatsPurchaseQuote? = null

    private val _purchases = MutableSharedFlow<SatsPurchase>()
    override val purchases = _purchases.asSharedFlow()

    private var _minSatsInAppProduct: PlayInAppProduct? = null
    override val minSatsInAppProduct: InAppProduct?
        get() = _minSatsInAppProduct?.let {
            InAppProduct(
                productId = it.productId,
                priceCurrencyCode = it.priceCurrencyCode,
                priceAmountMicros = it.priceAmountMicros,
            )
        }

    init {
        scope.launch { ensureBillingClientInitialized() }
    }

    private suspend fun ensureBillingClientInitialized(): Boolean =
        connectLock.withLock {
            val connectSuccess = if (!connected) connectBillingClient() else true
            connected = connectSuccess
            if (!connectSuccess) return false

            if (_minSatsInAppProduct == null) {
                initializeProducts()
            }
            return true
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun connectBillingClient(): Boolean {
        return try {
            withTimeout(5.seconds) {
                suspendCancellableCoroutine { continuation ->
                    billingClient.startConnection(
                        object : BillingClientStateListener {
                            override fun onBillingSetupFinished(billinResult: BillingResult) {
                                continuation.resume(
                                    value = billinResult.responseCode == BillingResponseCode.OK,
                                    onCancellation = { Timber.e(it) },
                                )
                            }

                            override fun onBillingServiceDisconnected() = Unit
                        },
                    )
                }
            }
        } catch (error: TimeoutCancellationException) {
            Timber.w(error)
            false
        }
    }

    private suspend fun initializeProducts() {
        val response = billingClient.queryProductDetails(minSatsQueryProductDetailsParams)
        val productDetails = response.productDetailsList?.firstOrNull()
        val offerDetails = productDetails?.oneTimePurchaseOfferDetails
        if (productDetails != null && offerDetails != null) {
            _minSatsInAppProduct = PlayInAppProduct(
                productId = productDetails.productId,
                title = productDetails.title,
                name = productDetails.name,
                priceAmountMicros = offerDetails.priceAmountMicros,
                priceCurrencyCode = offerDetails.priceCurrencyCode,
                formattedPrice = offerDetails.formattedPrice,
                googlePlayProductDetails = productDetails,
            )
        }
    }

    @Throws(BillingNotAvailable::class, ProductNotAvailable::class)
    override suspend fun launchMinSatsBillingFlow(quote: SatsPurchaseQuote, activity: Activity) {
        val initialized = ensureBillingClientInitialized()
        if (!initialized) throw BillingNotAvailable()

        val minSatsProduct = _minSatsInAppProduct ?: throw ProductNotAvailable()
        val minSatsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(minSatsProduct.googlePlayProductDetails)
                .build(),
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(minSatsParamsList)
            .build()

        purchaseQuote = quote
        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override suspend fun refreshMinSatsInAppProduct() {
        val initialized = ensureBillingClientInitialized()
        if (initialized) initializeProducts()
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        scope.launch {
            purchases?.forEach { purchase ->
                purchaseQuote?.let { quote ->
                    _purchases.emit(
                        SatsPurchase(
                            quote = quote,
                            orderId = purchase.orderId,
                            productId = MIN_SATS_PRODUCT_ID,
                            purchaseTime = purchase.purchaseTime,
                            purchaseToken = purchase.purchaseToken,
                            quantity = purchase.quantity,
                        ),
                    )
                }
            }
        }
    }

    companion object {
        private const val MIN_SATS_PRODUCT_ID = "minsats"
        private val minSatsQueryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(MIN_SATS_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build(),
                ),
            )
            .build()
    }
}
