package net.primal.wallet.data.remote.api

import io.github.aakira.napier.Napier
import kotlin.time.Duration.Companion.milliseconds
import kotlin.uuid.Uuid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.sockets.toPrimalSubscriptionId
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.CommonJsonEncodeDefaults
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.common.PrimalEvent
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.common.util.takeContentOrNull
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.domain.wallet.SubWallet
import net.primal.wallet.data.remote.PrimalWalletVerb
import net.primal.wallet.data.remote.WalletOperationVerb
import net.primal.wallet.data.remote.model.AppSpecificDataRequest
import net.primal.wallet.data.remote.model.BalanceRequestBody
import net.primal.wallet.data.remote.model.BalanceResponse
import net.primal.wallet.data.remote.model.DepositRequestBody
import net.primal.wallet.data.remote.model.ExchangeRateRequestBody
import net.primal.wallet.data.remote.model.InAppPurchaseQuoteRequestBody
import net.primal.wallet.data.remote.model.InAppPurchaseQuoteResponse
import net.primal.wallet.data.remote.model.InAppPurchaseRequestBody
import net.primal.wallet.data.remote.model.IsWalletUserRequestBody
import net.primal.wallet.data.remote.model.LightningInvoiceResponse
import net.primal.wallet.data.remote.model.MiningFeeTier
import net.primal.wallet.data.remote.model.MiningFeesRequestBody
import net.primal.wallet.data.remote.model.OnChainAddressResponse
import net.primal.wallet.data.remote.model.ParseLnInvoiceRequestBody
import net.primal.wallet.data.remote.model.ParseLnUrlRequestBody
import net.primal.wallet.data.remote.model.ParsedLnInvoiceResponse
import net.primal.wallet.data.remote.model.ParsedLnUrlResponse
import net.primal.wallet.data.remote.model.PromoCodeDetailsResponse
import net.primal.wallet.data.remote.model.PromoCodeRequestBody
import net.primal.wallet.data.remote.model.RegisterSparkPubkeyRequestBody
import net.primal.wallet.data.remote.model.TransactionsRequestBody
import net.primal.wallet.data.remote.model.TransactionsResponse
import net.primal.wallet.data.remote.model.UnregisterSparkPubkeyRequestBody
import net.primal.wallet.data.remote.model.UserWalletInfoRequestBody
import net.primal.wallet.data.remote.model.WalletRequestBody
import net.primal.wallet.data.remote.model.WalletStatusResponse
import net.primal.wallet.data.remote.model.WalletUserInfoResponse
import net.primal.wallet.data.remote.model.WithdrawRequestBody
import net.primal.wallet.data.remote.nostr.ContentWalletExchangeRate
import net.primal.wallet.data.remote.nostr.ContentWalletTransaction
import net.primal.wallet.data.remote.nostr.WalletUserInfoContent
import net.primal.wallet.data.remote.serialization.encodeToWalletJsonString

class PrimalWalletApiImpl(
    private val primalApiClient: PrimalApiClient,
    private val signatureHandler: NostrEventSignatureHandler,
) : PrimalWalletApi {

    override suspend fun getWalletStatus(userId: String): WalletStatusResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.GET_WALLET_STATUS.id,
                optionsJson = AppSpecificDataRequest(
                    eventFromUser = signatureHandler.signNostrEvent(
                        NostrUnsignedEvent(
                            pubKey = userId,
                            kind = NostrEventKind.ApplicationSpecificData.value,
                            tags = emptyList(),
                            content = "GetWalletStatus",
                        ),
                    ).unwrapOrThrow(),
                ).encodeToWalletJsonString(),
            ),
        )

        val nostrEvent = queryResult.findPrimalEvent(NostrEventKind.PrimalWalletStatusInfo)
        return nostrEvent?.content?.decodeFromJsonStringOrNull<WalletStatusResponse>()
            ?: throw NetworkException("Missing or invalid content in response.")
    }

    override suspend fun getWalletUserKycLevel(userId: String): Int {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.IS_USER,
                    requestBody = IsWalletUserRequestBody(userId),
                    signatureHandler = signatureHandler,
                ),
            ),
        )

        val isUserEvent = queryResult.findPrimalEvent(NostrEventKind.PrimalWalletIsUser)
        return isUserEvent?.content?.toIntOrNull() ?: throw NetworkException("Missing or invalid content in response.")
    }

    override suspend fun getWalletUserInfo(userId: String): WalletUserInfoResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.USER_INFO,
                    requestBody = UserWalletInfoRequestBody(userId),
                    signatureHandler = signatureHandler,
                ),
            ),
        )

        return queryResult
            .findPrimalEvent(NostrEventKind.PrimalWalletUserInfo)
            .toUserWalletInfoResponseOrThrow()
    }

    override suspend fun getBalance(userId: String): BalanceResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.BALANCE,
                    requestBody = BalanceRequestBody(subWallet = SubWallet.Open),
                    signatureHandler = signatureHandler,
                ),
            ),
        )

        return queryResult.findPrimalEvent(NostrEventKind.PrimalWalletBalance)
            ?.takeContentOrNull<BalanceResponse>()
            ?: throw NetworkException("Missing or invalid content in response.")
    }

    override suspend fun subscribeToBalance(userId: String): Flow<BalanceResponse> {
        val subscriptionId = Uuid.random().toPrimalSubscriptionId()
        return primalApiClient
            .subscribeBufferedOnInactivity(
                subscriptionId = subscriptionId,
                message = PrimalCacheFilter(
                    primalVerb = PrimalWalletVerb.WALLET_MONITOR.id,
                    optionsJson = CommonJsonEncodeDefaults.encodeToString(
                        WalletRequestBody(
                            event = signatureHandler.signNostrEvent(
                                unsignedNostrEvent = NostrUnsignedEvent(
                                    pubKey = userId,
                                    content = BalanceRequestBody(subWallet = SubWallet.Open).encodeToJsonString(),
                                    kind = NostrEventKind.PrimalWalletOperation.value,
                                    tags = listOf(),
                                ),
                            ).unwrapOrThrow(),
                        ),
                    ),
                ),
                inactivityTimeout = 300.milliseconds,
            ).mapNotNull {
                val primalEvent = it.findPrimalEvent(NostrEventKind.PrimalWalletBalance)
                primalEvent.takeContentOrNull<BalanceResponse>()
            }
    }

    override suspend fun withdraw(userId: String, body: WithdrawRequestBody) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.WITHDRAW,
                    requestBody = body,
                    signatureHandler = signatureHandler,
                ),
            ),
        )
    }

    override suspend fun createLightningInvoice(userId: String, body: DepositRequestBody): LightningInvoiceResponse {
        val response = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.DEPOSIT,
                    requestBody = body,
                    signatureHandler = signatureHandler,
                ),
            ),
        )

        return response.findPrimalEvent(NostrEventKind.PrimalWalletDepositInvoice)
            ?.takeContentOrNull<LightningInvoiceResponse>()
            ?: throw NetworkException("Missing or invalid content in response.")
    }

    override suspend fun createOnChainAddress(userId: String, body: DepositRequestBody): OnChainAddressResponse {
        val response = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.DEPOSIT,
                    requestBody = body,
                    signatureHandler = signatureHandler,
                ),
            ),
        )

        return response.findPrimalEvent(NostrEventKind.PrimalWalletOnChainAddress)
            ?.takeContentOrNull<OnChainAddressResponse>()
            ?: throw NetworkException("Missing or invalid content in response.")
    }

    override suspend fun getTransactions(userId: String, body: TransactionsRequestBody): TransactionsResponse {
        val result = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.TRANSACTIONS,
                    requestBody = body,
                    signatureHandler = signatureHandler,
                ),
            ),
        )

        val transactionsEvent = result.findPrimalEvent(kind = NostrEventKind.PrimalWalletTransactions)
            ?: throw NetworkException("Missing or invalid content in response.")

        val txJsonArray = transactionsEvent.content.decodeFromJsonStringOrNull<JsonArray>()
            ?: throw NetworkException("Invalid content in 10_000_304 event.")

        val transactions = txJsonArray.mapNotNull {
            try {
                CommonJson.decodeFromJsonElement<ContentWalletTransaction>(it)
            } catch (error: IllegalArgumentException) {
                Napier.w(error) { "Unable to decode " }
                null
            }
        }

        return TransactionsResponse(
            transactions = transactions,
            paging = result.findPrimalEvent(kind = NostrEventKind.PrimalPaging)
                ?.content?.decodeFromJsonStringOrNull(),
        )
    }

    override suspend fun getPromoCodeDetails(code: String): PromoCodeDetailsResponse {
        val result = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.PROMO_CODE_GET_DETAILS.id,
                optionsJson = PromoCodeRequestBody(
                    promoCode = code,
                ).encodeToJsonString(),
            ),
        )

        return result.findPrimalEvent(kind = NostrEventKind.PrimalPromoCodeDetails)
            .takeContentOrNull<PromoCodeDetailsResponse>()
            ?: throw NetworkException("Missing or invalid content in response.")
    }

    override suspend fun redeemPromoCode(authorizationEvent: NostrEvent) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.PROMO_CODES_REDEEM.id,
                optionsJson = AppSpecificDataRequest(
                    eventFromUser = authorizationEvent,
                ).encodeToJsonString(),
            ),
        )
    }

    override suspend fun getInAppPurchaseQuote(
        userId: String,
        productId: String,
        region: String,
        previousQuoteId: String?,
    ): InAppPurchaseQuoteResponse {
        val result = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.IN_APP_PURCHASE_QUOTE,
                    requestBody = InAppPurchaseQuoteRequestBody(
                        productId = productId,
                        region = region,
                        platform = "android",
                        quoteId = previousQuoteId,
                    ),
                    signatureHandler = signatureHandler,
                ),
            ),
        )

        val quoteEvent = result.findPrimalEvent(NostrEventKind.PrimalWalletInAppPurchaseQuote)
        return quoteEvent?.takeContentOrNull<InAppPurchaseQuoteResponse>()
            ?: throw NetworkException("Missing or invalid content in response.")
    }

    override suspend fun confirmInAppPurchase(
        userId: String,
        quoteId: String,
        purchaseToken: String,
    ) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.IN_APP_PURCHASE,
                    requestBody = InAppPurchaseRequestBody(purchaseToken = purchaseToken, quoteId = quoteId),
                    signatureHandler = signatureHandler,
                ),
            ),
        )
    }

    override suspend fun parseLnUrl(userId: String, lnurl: String): ParsedLnUrlResponse {
        val result = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.PARSE_LNURL,
                    requestBody = ParseLnUrlRequestBody(lnurl = lnurl),
                    signatureHandler = signatureHandler,
                ),
            ),
        )

        return result.findPrimalEvent(kind = NostrEventKind.PrimalWalletParsedLnUrl)
            ?.takeContentOrNull<ParsedLnUrlResponse>()
            ?: throw NetworkException("Missing or invalid content in response.")
    }

    override suspend fun parseLnInvoice(userId: String, lnbc: String): ParsedLnInvoiceResponse {
        val result = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.PARSE_LNINVOICE,
                    requestBody = ParseLnInvoiceRequestBody(lnbc = lnbc),
                    signatureHandler = signatureHandler,
                ),
            ),
        )

        return result.findPrimalEvent(kind = NostrEventKind.PrimalWalletParsedLnInvoice)
            ?.takeContentOrNull<ParsedLnInvoiceResponse>()
            ?: throw NetworkException("Missing or invalid content in response.")
    }

    override suspend fun getMiningFees(
        userId: String,
        onChainAddress: String,
        amountInBtc: String,
    ): List<MiningFeeTier> {
        val result = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.ONCHAIN_PAYMENT_TIERS,
                    requestBody = MiningFeesRequestBody(btcAddress = onChainAddress, amountInBtc = amountInBtc),
                    signatureHandler = signatureHandler,
                ),
            ),
        )

        return result.findPrimalEvent(kind = NostrEventKind.PrimalWalletMiningFees)
            ?.takeContentOrNull<List<MiningFeeTier>>()
            ?: throw NetworkException("Missing or invalid content in response.")
    }

    private fun PrimalEvent?.toUserWalletInfoResponseOrThrow(): WalletUserInfoResponse {
        val content = takeContentOrNull<WalletUserInfoContent>()
            ?: throw NetworkException("Missing or invalid content in response.")
        return WalletUserInfoResponse(
            kycLevel = content.kycLevel,
            lightningAddress = content.lud16,
        )
    }

    override suspend fun registerSparkWallet(userId: String, sparkWalletId: String) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.REGISTER_SPARK_PUBKEY,
                    requestBody = RegisterSparkPubkeyRequestBody(sparkPubkey = sparkWalletId),
                    signatureHandler = signatureHandler,
                ),
            ),
        )
    }

    override suspend fun unregisterSparkWallet(userId: String, sparkWalletId: String) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.UNREGISTER_SPARK_PUBKEY,
                    requestBody = UnregisterSparkPubkeyRequestBody(sparkPubkey = sparkWalletId),
                    signatureHandler = signatureHandler,
                ),
            ),
        )
    }

    override suspend fun getExchangeRate(userId: String): Double {
        val result = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalWalletVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.EXCHANGE_RATE,
                    requestBody = ExchangeRateRequestBody,
                    signatureHandler = signatureHandler,
                ),
            ),
        )

        return result.findPrimalEvent(NostrEventKind.PrimalWalletExchangeRate)
            ?.takeContentOrNull<ContentWalletExchangeRate>()?.rate
            ?: throw NetworkException("Missing or invalid content in response.")
    }
}
