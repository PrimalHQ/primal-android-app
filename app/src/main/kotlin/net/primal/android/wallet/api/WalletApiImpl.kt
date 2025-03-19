package net.primal.android.wallet.api

import javax.inject.Inject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.decodeFromJsonElement
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.networking.di.PrimalWalletApiClient
import net.primal.android.nostr.ext.takeContentOrNull
import net.primal.android.nostr.model.primal.content.ContentWalletExchangeRate
import net.primal.android.nostr.model.primal.content.ContentWalletTransaction
import net.primal.android.nostr.model.primal.content.WalletActivationContent
import net.primal.android.nostr.model.primal.content.WalletUserInfoContent
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.wallet.api.model.ActivateWalletRequestBody
import net.primal.android.wallet.api.model.BalanceRequestBody
import net.primal.android.wallet.api.model.BalanceResponse
import net.primal.android.wallet.api.model.DepositRequestBody
import net.primal.android.wallet.api.model.ExchangeRateRequestBody
import net.primal.android.wallet.api.model.GetActivationCodeRequestBody
import net.primal.android.wallet.api.model.InAppPurchaseQuoteRequestBody
import net.primal.android.wallet.api.model.InAppPurchaseQuoteResponse
import net.primal.android.wallet.api.model.InAppPurchaseRequestBody
import net.primal.android.wallet.api.model.IsWalletUserRequestBody
import net.primal.android.wallet.api.model.LightningInvoiceResponse
import net.primal.android.wallet.api.model.MiningFeeTier
import net.primal.android.wallet.api.model.MiningFeesRequestBody
import net.primal.android.wallet.api.model.OnChainAddressResponse
import net.primal.android.wallet.api.model.ParseLnInvoiceRequestBody
import net.primal.android.wallet.api.model.ParseLnUrlRequestBody
import net.primal.android.wallet.api.model.ParsedLnInvoiceResponse
import net.primal.android.wallet.api.model.ParsedLnUrlResponse
import net.primal.android.wallet.api.model.TransactionsRequestBody
import net.primal.android.wallet.api.model.TransactionsResponse
import net.primal.android.wallet.api.model.UserWalletInfoRequestBody
import net.primal.android.wallet.api.model.WalletOperationVerb
import net.primal.android.wallet.api.model.WalletUserInfoResponse
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.domain.SubWallet
import net.primal.core.networking.primal.PrimalApiClient
import net.primal.core.networking.primal.PrimalCacheFilter
import net.primal.core.networking.sockets.errors.WssException
import net.primal.data.remote.PrimalVerb
import net.primal.domain.PrimalEvent
import net.primal.domain.nostr.NostrEventKind
import timber.log.Timber

class WalletApiImpl @Inject constructor(
    @PrimalWalletApiClient private val primalApiClient: PrimalApiClient,
    private val nostrNotary: NostrNotary,
) : WalletApi {

    override suspend fun getWalletUserKycLevel(userId: String): Int {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.IS_USER,
                    requestBody = IsWalletUserRequestBody(userId),
                    nostrNotary = nostrNotary,
                ),
            ),
        )

        val isUserEvent = queryResult.findPrimalEvent(NostrEventKind.PrimalWalletIsUser)
        return isUserEvent?.content?.toIntOrNull() ?: throw WssException("Missing or invalid content in response.")
    }

    override suspend fun getWalletUserInfo(userId: String): WalletUserInfoResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.USER_INFO,
                    requestBody = UserWalletInfoRequestBody(userId),
                    nostrNotary = nostrNotary,
                ),
            ),
        )

        return queryResult
            .findPrimalEvent(NostrEventKind.PrimalWalletUserInfo)
            .toUserWalletInfoResponseOrThrow()
    }

    override suspend fun requestActivationCodeToEmail(userId: String, body: GetActivationCodeRequestBody) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.GET_ACTIVATION_CODE,
                    requestBody = body,
                    nostrNotary = nostrNotary,
                ),
            ),
        )
    }

    override suspend fun activateWallet(userId: String, code: String): String {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.ACTIVATE,
                    requestBody = ActivateWalletRequestBody(code),
                    nostrNotary = nostrNotary,
                ),
            ),
        )

        return queryResult
            .findPrimalEvent(NostrEventKind.PrimalWalletActivation)
            .toWalletLightningAddressOrThrow()
    }

    override suspend fun getBalance(userId: String): BalanceResponse {
        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.BALANCE,
                    requestBody = BalanceRequestBody(subWallet = SubWallet.Open),
                    nostrNotary = nostrNotary,
                ),
            ),
        )

        return queryResult.findPrimalEvent(NostrEventKind.PrimalWalletBalance)
            ?.takeContentOrNull<BalanceResponse>()
            ?: throw WssException("Missing or invalid content in response.")
    }

    override suspend fun withdraw(userId: String, body: WithdrawRequestBody) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.WITHDRAW,
                    requestBody = body,
                    nostrNotary = nostrNotary,
                ),
            ),
        )
    }

    override suspend fun createLightningInvoice(userId: String, body: DepositRequestBody): LightningInvoiceResponse {
        val response = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.DEPOSIT,
                    requestBody = body,
                    nostrNotary = nostrNotary,
                ),
            ),
        )

        return response.findPrimalEvent(NostrEventKind.PrimalWalletDepositInvoice)
            ?.takeContentOrNull<LightningInvoiceResponse>()
            ?: throw WssException("Missing or invalid content in response.")
    }

    override suspend fun createOnChainAddress(userId: String, body: DepositRequestBody): OnChainAddressResponse {
        val response = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.DEPOSIT,
                    requestBody = body,
                    nostrNotary = nostrNotary,
                ),
            ),
        )

        return response.findPrimalEvent(NostrEventKind.PrimalWalletOnChainAddress)
            ?.takeContentOrNull<OnChainAddressResponse>()
            ?: throw WssException("Missing or invalid content in response.")
    }

    override suspend fun getTransactions(userId: String, body: TransactionsRequestBody): TransactionsResponse {
        val result = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.TRANSACTIONS,
                    requestBody = body,
                    nostrNotary = nostrNotary,
                ),
            ),
        )

        val transactionsEvent = result.findPrimalEvent(kind = NostrEventKind.PrimalWalletTransactions)
            ?: throw WssException("Missing or invalid content in response.")

        val txJsonArray = NostrJson.decodeFromString<JsonArray>(transactionsEvent.content)
        val transactions = txJsonArray.mapNotNull {
            try {
                NostrJson.decodeFromJsonElement<ContentWalletTransaction>(it)
            } catch (error: IllegalArgumentException) {
                Timber.w(error)
                null
            }
        }

        return TransactionsResponse(
            transactions = transactions,
            paging = result.findPrimalEvent(kind = NostrEventKind.PrimalPaging)?.let {
                NostrJson.decodeFromStringOrNull(it.content)
            },
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
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.IN_APP_PURCHASE_QUOTE,
                    requestBody = InAppPurchaseQuoteRequestBody(
                        productId = productId,
                        region = region,
                        platform = "android",
                        quoteId = previousQuoteId,
                    ),
                    nostrNotary = nostrNotary,
                ),
            ),
        )

        val quoteEvent = result.findPrimalEvent(NostrEventKind.PrimalWalletInAppPurchaseQuote)
        return quoteEvent?.takeContentOrNull<InAppPurchaseQuoteResponse>()
            ?: throw WssException("Missing or invalid content in response.")
    }

    override suspend fun confirmInAppPurchase(
        userId: String,
        quoteId: String,
        purchaseToken: String,
    ) {
        primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.IN_APP_PURCHASE,
                    requestBody = InAppPurchaseRequestBody(purchaseToken = purchaseToken, quoteId = quoteId),
                    nostrNotary = nostrNotary,
                ),
            ),
        )
    }

    override suspend fun parseLnUrl(userId: String, lnurl: String): ParsedLnUrlResponse {
        val result = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.PARSE_LNURL,
                    requestBody = ParseLnUrlRequestBody(lnurl = lnurl),
                    nostrNotary = nostrNotary,
                ),
            ),
        )

        return result.findPrimalEvent(kind = NostrEventKind.PrimalWalletParsedLnUrl)
            ?.takeContentOrNull<ParsedLnUrlResponse>()
            ?: throw WssException("Missing or invalid content in response.")
    }

    override suspend fun parseLnInvoice(userId: String, lnbc: String): ParsedLnInvoiceResponse {
        val result = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.PARSE_LNINVOICE,
                    requestBody = ParseLnInvoiceRequestBody(lnbc = lnbc),
                    nostrNotary = nostrNotary,
                ),
            ),
        )

        return result.findPrimalEvent(kind = NostrEventKind.PrimalWalletParsedLnInvoice)
            ?.takeContentOrNull<ParsedLnInvoiceResponse>()
            ?: throw WssException("Missing or invalid content in response.")
    }

    override suspend fun getMiningFees(
        userId: String,
        onChainAddress: String,
        amountInBtc: String,
    ): List<MiningFeeTier> {
        val result = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.ONCHAIN_PAYMENT_TIERS,
                    requestBody = MiningFeesRequestBody(btcAddress = onChainAddress, amountInBtc = amountInBtc),
                    nostrNotary = nostrNotary,
                ),
            ),
        )

        return result.findPrimalEvent(kind = NostrEventKind.PrimalWalletMiningFees)
            ?.takeContentOrNull<List<MiningFeeTier>>()
            ?: throw WssException("Missing or invalid content in response.")
    }

    private fun PrimalEvent?.toUserWalletInfoResponseOrThrow(): WalletUserInfoResponse {
        val content = takeContentOrNull<WalletUserInfoContent>()
            ?: throw WssException("Missing or invalid content in response.")
        return WalletUserInfoResponse(
            kycLevel = content.kycLevel,
            lightningAddress = content.lud16,
        )
    }

    private fun PrimalEvent?.toWalletLightningAddressOrThrow(): String {
        val content = takeContentOrNull<WalletActivationContent>()
            ?: throw WssException("Missing or invalid content in response.")
        return content.lud16
    }

    override suspend fun getExchangeRate(userId: String): Double {
        val result = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.WALLET.id,
                optionsJson = buildWalletOptionsJson(
                    userId = userId,
                    walletVerb = WalletOperationVerb.EXCHANGE_RATE,
                    requestBody = ExchangeRateRequestBody,
                    nostrNotary = nostrNotary,
                ),
            ),
        )

        return result.findPrimalEvent(NostrEventKind.PrimalWalletExchangeRate)
            ?.takeContentOrNull<ContentWalletExchangeRate>()?.rate
            ?: throw WssException("Missing or invalid content in response.")
    }
}
