package net.primal.android.wallet.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.api.UsersApi
import net.primal.android.user.domain.PrimalWallet
import net.primal.android.wallet.api.WalletApi
import net.primal.android.wallet.api.mediator.WalletTransactionsMediator
import net.primal.android.wallet.api.model.DepositRequestBody
import net.primal.android.wallet.api.model.InAppPurchaseQuoteResponse
import net.primal.android.wallet.api.model.ParsedLnInvoiceResponse
import net.primal.android.wallet.api.model.ParsedLnUrlResponse
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.db.WalletTransaction
import net.primal.android.wallet.domain.SubWallet
import net.primal.android.wallet.domain.WalletKycLevel

@OptIn(ExperimentalPagingApi::class)
class WalletRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val accountsStore: UserAccountsStore,
    private val walletApi: WalletApi,
    private val usersApi: UsersApi,
    private val database: PrimalDatabase,
) {

    fun latestTransactions(userId: String) =
        createTransactionsPager(userId) {
            database.walletTransactions().latestTransactionsPaged()
        }.flow

    suspend fun fetchUserWalletInfoAndUpdateUserAccount(userId: String) {
        withContext(dispatcherProvider.io()) {
            val response = walletApi.getWalletUserInfo(userId)
            val kycLevel = WalletKycLevel.valueOf(response.kycLevel) ?: return@withContext
            storeWalletInfoLocally(userId = userId, kycLevel = kycLevel, lightningAddress = response.lightningAddress)
        }
    }

    suspend fun activateWallet(userId: String, code: String): String {
        return withContext(dispatcherProvider.io()) {
            walletApi.activateWallet(userId, code)
        }
    }

    suspend fun requestActivationCodeToEmail(
        userId: String,
        name: String,
        email: String,
    ) {
        withContext(dispatcherProvider.io()) {
            walletApi.requestActivationCodeToEmail(userId, name, email)
        }
    }

    suspend fun withdraw(userId: String, body: WithdrawRequestBody) {
        withContext(dispatcherProvider.io()) {
            walletApi.withdraw(userId, body)
        }
    }

    suspend fun deposit(
        userId: String,
        amountInBtc: String?,
        comment: String?,
    ): String {
        val invoice = withContext(dispatcherProvider.io()) {
            walletApi.deposit(
                userId = userId,
                body = DepositRequestBody(
                    subWallet = SubWallet.Open,
                    amountBtc = amountInBtc,
                    description = comment,
                ),
            )
        }
        return invoice
    }

    suspend fun getInAppPurchaseMinSatsQuote(
        userId: String,
        region: String,
        productId: String,
        previousQuoteId: String?,
    ): InAppPurchaseQuoteResponse {
        return withContext(dispatcherProvider.io()) {
            walletApi.getInAppPurchaseQuote(
                userId = userId,
                productId = productId,
                region = region,
                previousQuoteId = previousQuoteId,
            )
        }
    }

    suspend fun confirmInAppPurchase(
        userId: String,
        quoteId: String,
        purchaseToken: String,
    ) {
        return withContext(dispatcherProvider.io()) {
            walletApi.confirmInAppPurchase(
                userId = userId,
                quoteId = quoteId,
                purchaseToken = purchaseToken,
            )
        }
    }

    suspend fun parseLnUrl(userId: String, lnurl: String): ParsedLnUrlResponse {
        return walletApi.parseLnUrl(userId = userId, lnurl = lnurl)
    }

    suspend fun parseLnInvoice(userId: String, lnbc: String): ParsedLnInvoiceResponse {
        return walletApi.parseLnInvoice(userId = userId, lnbc = lnbc)
    }

    private suspend fun storeWalletInfoLocally(
        userId: String,
        kycLevel: WalletKycLevel,
        lightningAddress: String,
    ) {
        val primalWallet = PrimalWallet(kycLevel = kycLevel, lightningAddress = lightningAddress)
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(primalWallet = primalWallet)
        }
    }

    private fun createTransactionsPager(
        userId: String,
        pagingSourceFactory: () -> PagingSource<Int, WalletTransaction>,
    ) = Pager(
        config = PagingConfig(
            pageSize = 50,
            prefetchDistance = 100,
            initialLoadSize = 200,
            enablePlaceholders = true,
        ),
        remoteMediator = WalletTransactionsMediator(
            dispatchers = dispatcherProvider,
            accountsStore = accountsStore,
            userId = userId,
            database = database,
            walletApi = walletApi,
            usersApi = usersApi,
        ),
        pagingSourceFactory = pagingSourceFactory,
    )
}
