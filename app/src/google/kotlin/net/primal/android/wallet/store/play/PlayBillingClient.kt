package net.primal.android.wallet.store.play

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
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
import net.primal.android.wallet.store.PrimalBillingClient
import net.primal.android.wallet.store.domain.InAppProduct
import net.primal.android.wallet.store.domain.SatsPurchase
import net.primal.android.wallet.store.domain.SatsPurchaseQuote
import net.primal.android.wallet.store.domain.SubscriptionBillingPeriod
import net.primal.android.wallet.store.domain.SubscriptionProduct
import net.primal.android.wallet.store.domain.SubscriptionPurchase
import net.primal.android.wallet.store.domain.SubscriptionTier
import net.primal.core.utils.coroutines.DispatcherProvider
import timber.log.Timber

class PlayBillingClient @Inject constructor(
    @ApplicationContext appContext: Context,
    dispatchers: DispatcherProvider,
) : PrimalBillingClient, PurchasesUpdatedListener {

    private val scope = CoroutineScope(dispatchers.io())

    private val billingClient by lazy {
        BillingClient.newBuilder(appContext)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build(),
            )
            .build()
    }

    private var connected: Boolean = false
    private val connectLock: Mutex = Mutex()

    private var purchaseQuote: SatsPurchaseQuote? = null

    private val _satsPurchases = MutableSharedFlow<SatsPurchase>()
    override val satsPurchases = _satsPurchases.asSharedFlow()

    private val _subscriptionPurchases = MutableSharedFlow<SubscriptionPurchase>()
    override val subscriptionPurchases = _subscriptionPurchases.asSharedFlow()

    private var _minSatsInAppProduct: PlayInAppProduct? = null
    private var _subscriptionProducts: List<PlaySubscriptionProduct> = emptyList()

    init {
        scope.launch { ensureBillingClientInitialized() }
    }

    private suspend fun ensureBillingClientInitialized(): Boolean =
        connectLock.withLock {
            val connectSuccess = if (!connected) connectBillingClient() else true
            connected = connectSuccess
            if (!connectSuccess) return false

            if (_minSatsInAppProduct == null) {
                queryMinSatsInAppProduct()
            }

            if (_subscriptionProducts.isEmpty()) {
                queryPlaySubscriptionProducts()
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

    private suspend fun queryMinSatsInAppProduct(): PlayInAppProduct? {
        val response = billingClient.queryProductDetails(minSatsQueryProductDetailsParams)
        val productDetails = response.productDetailsList?.firstOrNull()
        val offerDetails = productDetails?.oneTimePurchaseOfferDetails
        return if (productDetails != null && offerDetails != null) {
            PlayInAppProduct(
                productId = productDetails.productId,
                title = productDetails.title,
                name = productDetails.name,
                priceAmountMicros = offerDetails.priceAmountMicros,
                priceCurrencyCode = offerDetails.priceCurrencyCode,
                formattedPrice = offerDetails.formattedPrice,
                googlePlayProductDetails = productDetails,
            )
        } else {
            null
        }
    }

    private suspend fun queryPlaySubscriptionProducts(): List<PlaySubscriptionProduct> {
        return queryAllSubscriptionDetails().mapNotNull { productDetails ->
            val offerDetails = productDetails.subscriptionOfferDetails?.firstOrNull()
            val pricing = offerDetails?.pricingPhases?.pricingPhaseList?.firstOrNull()
            val offerToken = offerDetails?.offerToken
            if (pricing != null && offerToken != null) {
                PlaySubscriptionProduct(
                    productId = productDetails.productId,
                    title = productDetails.title,
                    name = productDetails.name,
                    priceAmountMicros = pricing.priceAmountMicros,
                    priceCurrencyCode = pricing.priceCurrencyCode,
                    formattedPrice = pricing.formattedPrice,
                    googlePlayProductDetails = productDetails,
                    googlePlayOfferToken = offerToken,
                )
            } else {
                null
            }
        }
    }

    private suspend fun queryAllSubscriptionDetails(): List<ProductDetails> {
        val queryProducts = QueryProductDetailsParams.newBuilder()
            .setProductList(
                playSubscriptionIds.map { productId ->
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                },
            )
            .build()

        return billingClient.queryProductDetails(queryProducts).productDetailsList ?: emptyList()
    }

    override suspend fun querySubscriptionProducts(): List<SubscriptionProduct> {
        ensureBillingClientInitialized()
        if (_subscriptionProducts.isEmpty()) {
            _subscriptionProducts = queryPlaySubscriptionProducts()
        }

        return _subscriptionProducts.map {
            SubscriptionProduct(
                productId = it.productId,
                priceCurrencyCode = it.priceCurrencyCode,
                priceAmountMicros = it.priceAmountMicros,
                billingPeriod = if (it.productId.contains("monthly", ignoreCase = true)) {
                    SubscriptionBillingPeriod.Monthly
                } else {
                    SubscriptionBillingPeriod.Yearly
                },
                tier = if (it.productId.contains("premium", ignoreCase = true)) {
                    SubscriptionTier.PREMIUM
                } else if (it.productId.contains("pro", ignoreCase = true)) {
                    SubscriptionTier.PRO
                } else {
                    error("New tier added? Please implement tier resolving.")
                },
            )
        }
    }

    override suspend fun queryMinSatsProduct(): InAppProduct? {
        ensureBillingClientInitialized()
        if (_minSatsInAppProduct == null) {
            _minSatsInAppProduct = queryMinSatsInAppProduct()
        }

        return _minSatsInAppProduct?.let {
            InAppProduct(
                productId = it.productId,
                priceCurrencyCode = it.priceCurrencyCode,
                priceAmountMicros = it.priceAmountMicros,
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun queryActiveSubscriptions(): List<SubscriptionPurchase> {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        return try {
            withTimeout(5.seconds) {
                suspendCancellableCoroutine { continuation ->
                    billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                        if (billingResult.responseCode == BillingResponseCode.OK) {
                            continuation.resume(
                                value = purchases.mapNotNull {
                                    val productId = it.products.firstOrNull()
                                    if (productId != null && productId.isSubscriptionProductId()) {
                                        it.mapAsSubscriptionPurchase(productId = productId)
                                    } else {
                                        null
                                    }
                                },
                                onCancellation = { Timber.e(it) },
                            )
                        } else {
                            continuation.resume(
                                value = emptyList(),
                                onCancellation = { Timber.e(it) },
                            )
                        }
                    }
                }
            }
        } catch (error: TimeoutCancellationException) {
            Timber.w(error)
            emptyList()
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

    @Throws(BillingNotAvailable::class)
    override suspend fun launchSubscriptionBillingFlow(
        activity: Activity,
        subscriptionProduct: SubscriptionProduct,
        existingSubscription: SubscriptionPurchase?,
    ) {
        val initialized = ensureBillingClientInitialized()
        if (!initialized) throw BillingNotAvailable()

        val subscription = _subscriptionProducts.find { it.productId == subscriptionProduct.productId }
            ?: throw ProductNotAvailable()

        val subscriptionParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(subscription.googlePlayProductDetails)
                .setOfferToken(subscription.googlePlayOfferToken)
                .build(),
        )

        val billingFlowParamsBuilder = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(subscriptionParamsList)

        if (existingSubscription != null && existingSubscription.productId != subscriptionProduct.productId) {
            val updateParams = BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                .setOldPurchaseToken(existingSubscription.purchaseToken)
                .setSubscriptionReplacementMode(
                    BillingFlowParams.SubscriptionUpdateParams.ReplacementMode.WITH_TIME_PRORATION,
                )
                .build()
            billingFlowParamsBuilder.setSubscriptionUpdateParams(updateParams)
        }

        val billingFlowParams = billingFlowParamsBuilder.build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        scope.launch {
            purchases?.forEach { purchase ->
                val productId = purchase.products.firstOrNull()
                if (productId != null && productId.isSubscriptionProductId()) {
                    _subscriptionPurchases.emit(
                        purchase.mapAsSubscriptionPurchase(productId = productId),
                    )
                } else {
                    purchaseQuote?.let { quote ->
                        _satsPurchases.emit(
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
    }

    private fun Purchase.mapAsSubscriptionPurchase(productId: String) =
        SubscriptionPurchase(
            orderId = this.orderId,
            productId = productId,
            purchaseTime = this.purchaseTime,
            purchaseState = this.purchaseState,
            purchaseToken = this.purchaseToken,
            quantity = this.quantity,
            autoRenewing = this.isAutoRenewing,
            playSubscriptionJson = this.originalJson,
        )

    private fun String?.isSubscriptionProductId(): Boolean = this in playSubscriptionIds

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

        private const val PREMIUM_MONTHLY_PRODUCT_ID = "monthly_premium"
        private const val PREMIUM_YEARLY_PRODUCT_ID = "yearly_premium"
        private const val PRO_MONTHLY_PRODUCT_ID = "monthly_pro"
        private const val PRO_YEARLY_PRODUCT_ID = "yearly_pro"

        private val playSubscriptionIds = listOf(
            PREMIUM_MONTHLY_PRODUCT_ID,
            PREMIUM_YEARLY_PRODUCT_ID,
            PRO_MONTHLY_PRODUCT_ID,
            PRO_YEARLY_PRODUCT_ID,
        )
    }
}
