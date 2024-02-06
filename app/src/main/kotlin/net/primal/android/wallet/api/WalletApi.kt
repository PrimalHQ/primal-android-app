package net.primal.android.wallet.api

import net.primal.android.wallet.api.model.BalanceResponse
import net.primal.android.wallet.api.model.DepositRequestBody
import net.primal.android.wallet.api.model.InAppPurchaseQuoteResponse
import net.primal.android.wallet.api.model.LightningInvoiceResponse
import net.primal.android.wallet.api.model.OnChainAddressResponse
import net.primal.android.wallet.api.model.ParsedLnInvoiceResponse
import net.primal.android.wallet.api.model.ParsedLnUrlResponse
import net.primal.android.wallet.api.model.TransactionsRequestBody
import net.primal.android.wallet.api.model.TransactionsResponse
import net.primal.android.wallet.api.model.WalletUserInfoResponse
import net.primal.android.wallet.api.model.WithdrawRequestBody

interface WalletApi {

    suspend fun getWalletUserKycLevel(userId: String): Int

    suspend fun getWalletUserInfo(userId: String): WalletUserInfoResponse

    suspend fun requestActivationCodeToEmail(
        userId: String,
        name: String,
        email: String,
        country: String?,
        state: String?,
    )

    suspend fun activateWallet(userId: String, code: String): String

    suspend fun getBalance(userId: String): BalanceResponse

    suspend fun withdraw(userId: String, body: WithdrawRequestBody)

    suspend fun createLightningInvoice(userId: String, body: DepositRequestBody): LightningInvoiceResponse

    suspend fun createOnChainAddress(userId: String, body: DepositRequestBody): OnChainAddressResponse

    suspend fun getTransactions(userId: String, body: TransactionsRequestBody): TransactionsResponse

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
}
