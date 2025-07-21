package net.primal.wallet.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.map
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.wallet.LnInvoiceCreateResult
import net.primal.domain.wallet.LnInvoiceParseResult
import net.primal.domain.wallet.LnUrlParseResult
import net.primal.domain.wallet.Network
import net.primal.domain.wallet.OnChainAddressResult
import net.primal.domain.wallet.SubWallet
import net.primal.domain.wallet.TransactionWithProfile
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletPayParams
import net.primal.domain.wallet.WalletRepository
import net.primal.wallet.data.service.WalletService
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.db.withTransaction
import net.primal.wallet.data.local.dao.NostrWalletData
import net.primal.wallet.data.local.dao.WalletInfo
import net.primal.wallet.data.local.dao.WalletSettings
import net.primal.wallet.data.local.dao.WalletTransactionData
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.remote.model.DepositRequestBody
import net.primal.wallet.data.repository.mappers.local.asWalletTransactionDO
import net.primal.wallet.data.repository.mappers.local.toDomain
import net.primal.wallet.data.repository.mappers.local.toWithdrawRequestDTO
import net.primal.wallet.data.repository.mappers.remote.asLightingInvoiceResultDO
import net.primal.wallet.data.repository.transactions.WalletTransactionsMediator

@OptIn(ExperimentalPagingApi::class)
internal class WalletRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val primalWalletService: WalletService,
    private val nostrWalletService: WalletService,
    private val primalWalletApi: PrimalWalletApi,
    private val walletDatabase: WalletDatabase,
    private val profileRepository: ProfileRepository,
) : WalletRepository {

    override suspend fun upsertWalletSettings(walletId: String, spamThresholdAmountInSats: Long) =
        withContext(dispatcherProvider.io()) {
            walletDatabase.walletSettings().upsertWalletSettings(
                WalletSettings(
                    walletId = walletId,
                    spamThresholdAmountInSats = spamThresholdAmountInSats,
                ),
            )
        }

    override suspend fun upsertNostrWallet(userId: String, wallet: Wallet.NWC) =
        withContext(dispatcherProvider.io()) {
            walletDatabase.withTransaction {
                walletDatabase.wallet().upsertWalletInfo(
                    info = WalletInfo(
                        walletId = wallet.walletId,
                        userId = userId,
                        lightningAddress = wallet.lightningAddress,
                        type = WalletType.NWC,
                        balanceInBtc = wallet.balanceInBtc,
                        maxBalanceInBtc = wallet.maxBalanceInBtc,
                        lastUpdatedAt = wallet.lastUpdatedAt,
                    ),
                )

                walletDatabase.wallet().upsertNostrWalletData(
                    data = NostrWalletData(
                        walletId = wallet.walletId,
                        relays = wallet.relays,
                        walletPubkey = wallet.keypair.pubKey,
                        walletPrivateKey = wallet.keypair.privateKey,
                    ),
                )
            }
        }

    override fun latestTransactions(userId: String): Flow<PagingData<TransactionWithProfile>> {
        return createTransactionsPager(userId) {
            walletDatabase.walletTransactions().latestTransactionsPagedByUserId(userId = userId)
        }.flow.map {
            it.map { txData ->
                val otherProfile = txData.otherUserId?.let { profileId ->
                    profileRepository.findProfileDataOrNull(profileId)
                }
                TransactionWithProfile(
                    transaction = txData.asWalletTransactionDO(),
                    otherProfileData = otherProfile,
                )
            }
        }
    }

    override suspend fun findTransactionByIdOrNull(txId: String): TransactionWithProfile? =
        withContext(dispatcherProvider.io()) {
            val transaction = walletDatabase.walletTransactions().findTransactionById(txId = txId)
                ?: return@withContext null

            val profile = transaction.otherUserId
                ?.let { profileRepository.findProfileDataOrNull(profileId = it) }

            TransactionWithProfile(
                transaction = transaction.asWalletTransactionDO(),
                otherProfileData = profile,
            )
        }

    override suspend fun pay(params: WalletPayParams) {
        withContext(dispatcherProvider.io()) {
            primalWalletApi.withdraw(
                userId = params.userId,
                body = params.toWithdrawRequestDTO(),
            )
        }
    }

    override suspend fun createLightningInvoice(
        userId: String,
        amountInBtc: String?,
        comment: String?,
    ): LnInvoiceCreateResult {
        return withContext(dispatcherProvider.io()) {
            val response = primalWalletApi.createLightningInvoice(
                userId = userId,
                body = DepositRequestBody(
                    subWallet = SubWallet.Open,
                    amountBtc = amountInBtc,
                    description = comment,
                ),
            )
            response.asLightingInvoiceResultDO()
        }
    }

    override suspend fun createOnChainAddress(userId: String): OnChainAddressResult {
        return withContext(dispatcherProvider.io()) {
            val response = primalWalletApi.createOnChainAddress(
                userId = userId,
                body = DepositRequestBody(subWallet = SubWallet.Open, network = Network.Bitcoin),
            )
            OnChainAddressResult(address = response.onChainAddress)
        }
    }

    override suspend fun fetchWalletBalance(walletId: String): Result<Unit> =
        withContext(dispatcherProvider.io()) {
            val wallet = walletDatabase.wallet().findWallet(walletId = walletId)
                ?: return@withContext Result.failure(
                    exception = IllegalArgumentException("Couldn't find wallet with the given walletId."),
                )

            when (wallet.info.type) {
                WalletType.PRIMAL -> primalWalletService.fetchWalletBalance(wallet = wallet.toDomain())
                WalletType.NWC -> nostrWalletService.fetchWalletBalance(wallet = wallet.toDomain())
            }.map { response ->
                walletDatabase.wallet().updateWalletBalance(
                    walletId = walletId,
                    balanceInBtc = response.balanceInBtc,
                    maxBalanceInBtc = response.maxBalanceInBtc,
                )
            }
        }

    override suspend fun parseLnUrl(userId: String, lnurl: String): LnUrlParseResult {
        return withContext(dispatcherProvider.io()) {
            val response = primalWalletApi.parseLnUrl(userId = userId, lnurl = lnurl)
            LnUrlParseResult(
                minSendable = response.minSendable,
                maxSendable = response.maxSendable,
                description = response.description,
                targetPubkey = response.targetPubkey,
                targetLud16 = response.targetLud16,
            )
        }
    }

    override suspend fun parseLnInvoice(userId: String, lnbc: String): LnInvoiceParseResult {
        return withContext(dispatcherProvider.io()) {
            val response = primalWalletApi.parseLnInvoice(userId = userId, lnbc = lnbc)
            LnInvoiceParseResult(
                userId = response.userId,
                comment = response.comment,
                amountMilliSats = response.lnInvoiceData.amountMilliSats,
                description = response.lnInvoiceData.description,
                date = response.lnInvoiceData.date,
                expiry = response.lnInvoiceData.expiry,
            )
        }
    }

    override suspend fun deleteAllTransactions(userId: String) =
        withContext(dispatcherProvider.io()) {
            walletDatabase.walletTransactions().deleteAllTransactionsByUserId(userId = userId)
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
            walletDatabase = walletDatabase,
            primalWalletApi = primalWalletApi,
            profileRepository = profileRepository,
        ),
        pagingSourceFactory = pagingSourceFactory,
    )
}
