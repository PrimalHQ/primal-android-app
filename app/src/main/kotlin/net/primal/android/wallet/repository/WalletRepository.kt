package net.primal.android.wallet.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import javax.inject.Inject
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.db.UsersDatabase
import net.primal.android.user.domain.PrimalWallet
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.api.WalletApi
import net.primal.android.wallet.api.mediator.WalletTransactionsMediator
import net.primal.android.wallet.api.model.DepositRequestBody
import net.primal.android.wallet.api.model.GetActivationCodeRequestBody
import net.primal.android.wallet.api.model.InAppPurchaseQuoteResponse
import net.primal.android.wallet.api.model.LightningInvoiceResponse
import net.primal.android.wallet.api.model.MiningFeeTier
import net.primal.android.wallet.api.model.OnChainAddressResponse
import net.primal.android.wallet.api.model.ParsedLnInvoiceResponse
import net.primal.android.wallet.api.model.ParsedLnUrlResponse
import net.primal.android.wallet.api.model.WithdrawRequestBody
import net.primal.android.wallet.db.WalletTransactionData
import net.primal.android.wallet.domain.Network
import net.primal.android.wallet.domain.SubWallet
import net.primal.android.wallet.domain.WalletKycLevel

@OptIn(ExperimentalPagingApi::class)
class WalletRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val accountsStore: UserAccountsStore,
    private val walletApi: WalletApi,
    private val usersDatabase: UsersDatabase,
    private val userRepository: UserRepository,
) {

    fun latestTransactions(userId: String) =
        createTransactionsPager(userId) {
            usersDatabase.walletTransactions().latestTransactionsPagedByUserId(userId = userId)
        }.flow

    fun findTransactionById(txId: String) = usersDatabase.walletTransactions().findTransactionById(txId = txId)

    suspend fun fetchUserWalletInfoAndUpdateUserAccount(userId: String) {
        val response = walletApi.getWalletUserInfo(userId)
        val kycLevel = WalletKycLevel.valueOf(response.kycLevel) ?: return
        storeWalletInfoLocally(userId = userId, kycLevel = kycLevel, lightningAddress = response.lightningAddress)
    }

    suspend fun activateWallet(userId: String, code: String): String {
        return walletApi.activateWallet(userId, code)
    }

    suspend fun requestActivationCodeToEmail(userId: String, body: GetActivationCodeRequestBody) {
        withContext(dispatcherProvider.io()) {
            walletApi.requestActivationCodeToEmail(userId, body)
        }
    }

    suspend fun withdraw(userId: String, body: WithdrawRequestBody) {
        withContext(dispatcherProvider.io()) {
            walletApi.withdraw(userId, body)
        }
    }

    suspend fun createLightningInvoice(
        userId: String,
        amountInBtc: String?,
        comment: String?,
    ): LightningInvoiceResponse {
        return withContext(dispatcherProvider.io()) {
            walletApi.createLightningInvoice(
                userId = userId,
                body = DepositRequestBody(subWallet = SubWallet.Open, amountBtc = amountInBtc, description = comment),
            )
        }
    }

    suspend fun generateOnChainAddress(userId: String): OnChainAddressResponse {
        return withContext(dispatcherProvider.io()) {
            walletApi.createOnChainAddress(
                userId = userId,
                body = DepositRequestBody(subWallet = SubWallet.Open, network = Network.Bitcoin),
            )
        }
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

    suspend fun fetchWalletBalance(userId: String) {
        val response = withContext(dispatcherProvider.io()) { walletApi.getBalance(userId = userId) }
        userRepository.updatePrimalWalletBalance(
            userId = userId,
            balanceInBtc = response.amount,
            maxBalanceInBtc = response.maxAmount,
        )
    }

    suspend fun getExchangeRate(userId: String) =
        withContext(dispatcherProvider.io()) { walletApi.getExchangeRate(userId) }

    suspend fun parseLnUrl(userId: String, lnurl: String): ParsedLnUrlResponse {
        return walletApi.parseLnUrl(userId = userId, lnurl = lnurl)
    }

    suspend fun parseLnInvoice(userId: String, lnbc: String): ParsedLnInvoiceResponse {
        return walletApi.parseLnInvoice(userId = userId, lnbc = lnbc)
    }

    fun deleteAllTransactions(userId: String) =
        usersDatabase.walletTransactions().deleteAllTransactionsByUserId(userId = userId)

    suspend fun fetchMiningFees(
        userId: String,
        onChainAddress: String,
        amountInBtc: String,
    ): List<MiningFeeTier> =
        withContext(dispatcherProvider.io()) {
            walletApi.getMiningFees(userId = userId, onChainAddress = onChainAddress, amountInBtc = amountInBtc)
        }

    suspend fun fetchDefaultMiningFee(
        userId: String,
        onChainAddress: String,
        amountInBtc: String,
    ): MiningFeeTier? =
        withContext(dispatcherProvider.io()) {
            val tiers = fetchMiningFees(
                userId = userId,
                onChainAddress = onChainAddress,
                amountInBtc = amountInBtc,
            )

            tiers.lastOrNull {
                it.id.contains("standard", ignoreCase = true)
            } ?: tiers.lastOrNull {
                it.id.contains("fast", ignoreCase = true)
            } ?: tiers.lastOrNull()
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
        pagingSourceFactory: () -> PagingSource<Int, WalletTransactionData>,
    ) = Pager(
        config = PagingConfig(
            pageSize = 50,
            prefetchDistance = 100,
            initialLoadSize = 200,
            enablePlaceholders = true,
        ),
        remoteMediator = WalletTransactionsMediator(
            userId = userId,
            dispatcherProvider = dispatcherProvider,
            accountsStore = accountsStore,
            usersDatabase = usersDatabase,
            walletApi = walletApi,
        ),
        pagingSourceFactory = pagingSourceFactory,
    )
}
