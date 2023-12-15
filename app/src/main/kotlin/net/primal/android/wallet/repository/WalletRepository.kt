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
import net.primal.android.user.domain.WalletPreference
import net.primal.android.wallet.api.WalletApi
import net.primal.android.wallet.api.mediator.WalletTransactionsMediator
import net.primal.android.wallet.api.model.InAppPurchaseQuoteResponse
import net.primal.android.wallet.api.model.WalletUserInfoResponse
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.db.WalletTransaction
import net.primal.android.wallet.domain.WalletKycLevel
import net.primal.android.wallet.store.play.BillingClientHandler

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
            val walletInfo = walletApi.getWalletUserInfo(userId)
            walletInfo.storeWalletInfoLocally(userId = userId)
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

    suspend fun updateWalletPreference(userId: String, walletPreference: WalletPreference) {
        accountsStore.getAndUpdateAccount(userId = userId) {
            copy(walletPreference = walletPreference)
        }
    }

    suspend fun getInAppPurchaseMinSatsQuote(userId: String, region: String): InAppPurchaseQuoteResponse {
        return withContext(dispatcherProvider.io()) {
            walletApi.getInAppPurchaseQuote(
                userId = userId,
                productId = BillingClientHandler.MIN_SATS_PRODUCT_ID.uppercase(),
                region = region,
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

    suspend fun parseLnUrl(userId: String, lnurl: String) {
        walletApi.parseLnUrl(userId = userId, lnurl = lnurl)
    }

    suspend fun parseLnInvoice(userId: String, lnbc: String) {
        walletApi.parseLnInvoice(userId = userId, lnbc = lnbc)
    }

    private suspend fun WalletUserInfoResponse.storeWalletInfoLocally(userId: String) {
        val kycLevel = WalletKycLevel.valueOf(kycLevel) ?: return
        val primalWallet = PrimalWallet(kycLevel = kycLevel, lightningAddress = this.lightningAddress)
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
