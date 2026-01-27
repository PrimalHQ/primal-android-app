package net.primal.wallet.data.remote.api

import kotlinx.coroutines.flow.Flow
import net.primal.domain.nostr.NostrEvent
import net.primal.wallet.data.remote.model.BalanceResponse
import net.primal.wallet.data.remote.model.DepositRequestBody
import net.primal.wallet.data.remote.model.InAppPurchaseQuoteResponse
import net.primal.wallet.data.remote.model.LightningInvoiceResponse
import net.primal.wallet.data.remote.model.MiningFeeTier
import net.primal.wallet.data.remote.model.OnChainAddressResponse
import net.primal.wallet.data.remote.model.ParsedLnInvoiceResponse
import net.primal.wallet.data.remote.model.ParsedLnUrlResponse
import net.primal.wallet.data.remote.model.PromoCodeDetailsResponse
import net.primal.wallet.data.remote.model.TransactionsRequestBody
import net.primal.wallet.data.remote.model.TransactionsResponse
import net.primal.wallet.data.remote.model.WalletUserInfoResponse
import net.primal.wallet.data.remote.model.WithdrawRequestBody

interface PrimalWalletApi {

    suspend fun getWalletUserKycLevel(userId: String): Int

    suspend fun getWalletUserInfo(userId: String): WalletUserInfoResponse

    suspend fun getBalance(userId: String): BalanceResponse

    suspend fun subscribeToBalance(userId: String): Flow<BalanceResponse>

    suspend fun withdraw(userId: String, body: WithdrawRequestBody)

    suspend fun createLightningInvoice(userId: String, body: DepositRequestBody): LightningInvoiceResponse

    suspend fun createOnChainAddress(userId: String, body: DepositRequestBody): OnChainAddressResponse

    suspend fun getTransactions(userId: String, body: TransactionsRequestBody): TransactionsResponse

    suspend fun getPromoCodeDetails(code: String): PromoCodeDetailsResponse

    suspend fun redeemPromoCode(authorizationEvent: NostrEvent)

    suspend fun getInAppPurchaseQuote(
        userId: String,
        productId: String,
        region: String,
        previousQuoteId: String? = null,
    ): InAppPurchaseQuoteResponse

    suspend fun confirmInAppPurchase(
        userId: String,
        quoteId: String,
        purchaseToken: String,
    )

    suspend fun parseLnUrl(userId: String, lnurl: String): ParsedLnUrlResponse

    suspend fun parseLnInvoice(userId: String, lnbc: String): ParsedLnInvoiceResponse

    suspend fun getMiningFees(
        userId: String,
        onChainAddress: String,
        amountInBtc: String,
    ): List<MiningFeeTier>

    suspend fun getExchangeRate(userId: String): Double
}
