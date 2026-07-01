package net.primal.wallet.data.remote.api

import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.common.util.takeContentOrNull
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.NostrEventSignatureHandler
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.wallet.data.remote.PrimalWalletVerb
import net.primal.wallet.data.remote.WalletOperationVerb
import net.primal.wallet.data.remote.model.AppSpecificDataRequest
import net.primal.wallet.data.remote.model.ExchangeRateRequestBody
import net.primal.wallet.data.remote.model.InAppPurchaseQuoteRequestBody
import net.primal.wallet.data.remote.model.InAppPurchaseQuoteResponse
import net.primal.wallet.data.remote.model.InAppPurchaseRequestBody
import net.primal.wallet.data.remote.model.ParseLnInvoiceRequestBody
import net.primal.wallet.data.remote.model.ParseLnUrlRequestBody
import net.primal.wallet.data.remote.model.ParsedLnInvoiceResponse
import net.primal.wallet.data.remote.model.ParsedLnUrlResponse
import net.primal.wallet.data.remote.model.RegisterSparkPubkeyRequestBody
import net.primal.wallet.data.remote.model.UnregisterSparkPubkeyRequestBody
import net.primal.wallet.data.remote.model.WalletStatusResponse
import net.primal.wallet.data.remote.nostr.ContentWalletExchangeRate
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
