package net.primal.wallet.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.map
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.LnInvoiceCreateRequest
import net.primal.domain.wallet.LnInvoiceCreateResult
import net.primal.domain.wallet.LnInvoiceParseResult
import net.primal.domain.wallet.LnUrlParseResult
import net.primal.domain.wallet.OnChainAddressResult
import net.primal.domain.wallet.TxRequest
import net.primal.domain.wallet.TxType
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletRepository
import net.primal.domain.wallet.WalletType
import net.primal.domain.wallet.exception.WalletException
import net.primal.domain.wallet.model.WalletBalanceResult
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.handler.TransactionsHandler
import net.primal.wallet.data.local.dao.NostrWalletData
import net.primal.wallet.data.local.dao.Wallet as WalletPO
import net.primal.wallet.data.local.dao.WalletInfo
import net.primal.wallet.data.local.dao.WalletSettings
import net.primal.wallet.data.local.dao.WalletTransactionData
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.repository.mappers.local.toDomain
import net.primal.wallet.data.repository.transactions.TimestampBasedWalletTransactionsMediator
import net.primal.wallet.data.service.WalletService
import net.primal.wallet.data.service.factory.WalletServiceFactory

@OptIn(ExperimentalPagingApi::class)
internal class WalletRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val walletServiceFactory: WalletServiceFactory,
    private val primalWalletApi: PrimalWalletApi,
    private val walletDatabase: WalletDatabase,
    private val profileRepository: ProfileRepository,
) : WalletRepository {

    private val transactionsHandler = TransactionsHandler(
        dispatchers = dispatcherProvider,
        walletServiceFactory = walletServiceFactory,
        walletDatabase = walletDatabase,
        profileRepository = profileRepository,
    )

    override suspend fun upsertWalletSettings(walletId: String, spamThresholdAmountInSats: Long) =
        withContext(dispatcherProvider.io()) {
            walletDatabase.walletSettings().upsertWalletSettings(
                WalletSettings(
                    walletId = walletId,
                    spamThresholdAmountInSats = spamThresholdAmountInSats.asEncryptable(),
                ),
            )
        }

    override suspend fun getWalletById(walletId: String): Result<Wallet> =
        withContext(dispatcherProvider.io()) {
            walletDatabase.wallet().findWallet(walletId = walletId)?.toDomain<Wallet>()
                ?.let { Result.success(it) }
                ?: Result.failure(WalletException.WalletNotFound())
        }

    override suspend fun deleteWalletById(walletId: String) =
        withContext(dispatcherProvider.io()) {
            walletDatabase.wallet().deleteWalletsByIds(walletIds = listOf(walletId))
        }

    override suspend fun upsertNostrWallet(userId: String, wallet: Wallet.NWC) =
        withContext(dispatcherProvider.io()) {
            walletDatabase.withTransaction {
                walletDatabase.wallet().upsertWalletInfo(
                    info = WalletInfo(
                        walletId = wallet.walletId,
                        userId = userId,
                        lightningAddress = wallet.lightningAddress?.asEncryptable(),
                        type = WalletType.NWC,
                        balanceInBtc = wallet.balanceInBtc?.asEncryptable(),
                        maxBalanceInBtc = wallet.maxBalanceInBtc?.asEncryptable(),
                        lastUpdatedAt = wallet.lastUpdatedAt,
                    ),
                )

                walletDatabase.wallet().upsertNostrWalletData(
                    data = NostrWalletData(
                        walletId = wallet.walletId,
                        relays = wallet.relays.asEncryptable(),
                        pubkey = wallet.pubkey.asEncryptable(),
                        walletPubkey = wallet.keypair.pubkey.asEncryptable(),
                        walletPrivateKey = wallet.keypair.privateKey.asEncryptable(),
                    ),
                )
            }
        }

    override fun latestTransactions(walletId: String): Flow<PagingData<Transaction>> {
        return createTransactionsPager(walletId) {
            walletDatabase.walletTransactions().latestTransactionsPagedByWalletId(walletId = walletId)
        }.flow.mapNotNull {
            it.map { txData ->
                val otherProfile = txData.otherUserId?.let { profileId ->
                    profileRepository.findProfileDataOrNull(profileId.decrypted)
                }

                txData.toDomain(otherProfile = otherProfile)
            }
        }
    }

    override suspend fun latestTransactions(walletId: String, limit: Int): List<Transaction> =
        withContext(dispatcherProvider.io()) {
            walletDatabase.walletTransactions()
                .latestTransactionsByWalletId(walletId = walletId, limit = limit)
                .map { txData ->
                    val otherProfile = txData.otherUserId?.let { profileId ->
                        profileRepository.findProfileDataOrNull(profileId.decrypted)
                    }

                    txData.toDomain(otherProfile = otherProfile)
                }
        }

    override suspend fun queryTransactions(
        walletId: String,
        type: TxType?,
        limit: Int,
        offset: Int,
        from: Long?,
        until: Long?,
    ): List<Transaction> =
        withContext(dispatcherProvider.io()) {
            walletDatabase.walletTransactions()
                .queryTransactions(
                    walletId = walletId,
                    type = type,
                    limit = limit,
                    offset = offset,
                    from = from,
                    until = until,
                )
                .map { txData ->
                    val otherProfile = txData.otherUserId?.let { profileId ->
                        profileRepository.findProfileDataOrNull(profileId.decrypted)
                    }

                    txData.toDomain(otherProfile = otherProfile)
                }
        }

    override suspend fun findTransactionByIdOrNull(txId: String): Transaction? =
        withContext(dispatcherProvider.io()) {
            val transaction = walletDatabase.walletTransactions().findTransactionById(txId = txId)
                ?: return@withContext null

            val profile = transaction.otherUserId
                ?.let { profileRepository.findProfileDataOrNull(profileId = it.decrypted) }

            transaction.toDomain(otherProfile = profile)
        }

    override suspend fun deleteAllTransactions(userId: String) =
        withContext(dispatcherProvider.io()) {
            walletDatabase.walletTransactions().deleteAllTransactions(userId = userId)
        }

    override suspend fun deleteAllUserData(userId: String) =
        withContext(dispatcherProvider.io()) {
            walletDatabase.withTransaction {
                val wallets = walletDatabase.wallet().findWalletInfosByUserId(userId = userId)
                val walletIds = wallets.map { it.walletId }

                if (walletIds.isNotEmpty()) {
                    walletDatabase.walletSettings().deleteWalletSettings(walletIds)
                    walletDatabase.wallet().deleteWalletsByIds(walletIds)
                }

                walletDatabase.walletTransactions().deleteAllTransactions(userId = userId)
                walletDatabase.wallet().clearActiveWallet(userId)
            }
        }

    override suspend fun pay(walletId: String, request: TxRequest): Result<Unit> =
        withContext(dispatcherProvider.io()) {
            val wallet = walletDatabase.wallet().findWallet(walletId = walletId)
                ?: return@withContext Result.failure(WalletException.WalletNotFound())

            wallet.resolveWalletService().pay(
                wallet = wallet.toDomain(),
                request = request,
            )
        }

    override suspend fun createLightningInvoice(
        walletId: String,
        amountInBtc: String?,
        comment: String?,
    ): Result<LnInvoiceCreateResult> {
        return withContext(dispatcherProvider.io()) {
            val wallet = walletDatabase.wallet().findWallet(walletId = walletId)
                ?: return@withContext Result.failure(WalletException.WalletNotFound())

            wallet.resolveWalletService().createLightningInvoice(
                wallet = wallet.toDomain(),
                request = LnInvoiceCreateRequest(
                    description = comment,
                    amountInBtc = amountInBtc,
                    expiry = if (wallet.info.type != WalletType.PRIMAL) 1.hours.inWholeSeconds else null,
                ),
            )
        }
    }

    override suspend fun createOnChainAddress(walletId: String): Result<OnChainAddressResult> {
        return withContext(dispatcherProvider.io()) {
            val wallet = walletDatabase.wallet().findWallet(walletId = walletId)
                ?: return@withContext Result.failure(WalletException.WalletNotFound())

            wallet.resolveWalletService().createOnChainAddress(wallet = wallet.toDomain())
        }
    }

    override suspend fun fetchWalletBalance(walletId: String): Result<Unit> =
        withContext(dispatcherProvider.io()) {
            val wallet = walletDatabase.wallet().findWallet(walletId = walletId)
                ?: return@withContext Result.failure(WalletException.WalletNotFound())

            wallet.resolveWalletService()
                .fetchWalletBalance(wallet = wallet.toDomain())
                .map { response ->
                    walletDatabase.wallet().updateWalletBalance(
                        walletId = walletId,
                        balanceInBtc = response.balanceInBtc.asEncryptable(),
                        maxBalanceInBtc = response.maxBalanceInBtc?.asEncryptable(),
                    )
                }
        }

    override suspend fun subscribeToWalletBalance(walletId: String): Flow<WalletBalanceResult> =
        flow {
            val wallet = walletDatabase.wallet().findWallet(walletId = walletId)
                ?: throw IllegalArgumentException("Couldn't find wallet with the given walletId.")

            wallet.resolveWalletService()
                .subscribeToWalletBalance(wallet = wallet.toDomain())
                .collect { balanceResult ->
                    updateWalletBalance(
                        walletId = walletId,
                        balanceInBtc = balanceResult.balanceInBtc,
                        maxBalanceInBtc = balanceResult.maxBalanceInBtc,
                    )
                    emit(balanceResult)
                }
        }.flowOn(dispatcherProvider.io())

    override suspend fun updateWalletBalance(
        walletId: String,
        balanceInBtc: Double,
        maxBalanceInBtc: Double?,
    ) = withContext(dispatcherProvider.io()) {
        walletDatabase.wallet().updateWalletBalance(
            walletId = walletId,
            balanceInBtc = balanceInBtc.asEncryptable(),
            maxBalanceInBtc = maxBalanceInBtc?.asEncryptable(),
        )
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
                paymentHash = response.lnInvoiceData.paymentHash,
            )
        }
    }

    private fun WalletPO.resolveWalletService(): WalletService<Wallet> {
        return walletServiceFactory.getServiceForWallet(this.toDomain())
    }

    private fun createTransactionsPager(
        walletId: String,
        pagingSourceFactory: () -> PagingSource<Int, WalletTransactionData>,
    ) = Pager(
        config = PagingConfig(
            pageSize = 50,
            prefetchDistance = 100,
            initialLoadSize = 200,
            enablePlaceholders = true,
        ),
        remoteMediator = TimestampBasedWalletTransactionsMediator(
            walletId = walletId,
            dispatcherProvider = dispatcherProvider,
            transactionsHandler = transactionsHandler,
            walletDatabase = walletDatabase,
        ),
        pagingSourceFactory = pagingSourceFactory,
    )
}
