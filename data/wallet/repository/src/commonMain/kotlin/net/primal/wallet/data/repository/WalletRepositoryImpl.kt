package net.primal.wallet.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.map
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.withContext
import net.primal.core.utils.CurrencyConversionUtils.btcToMSats
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.map
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.transactions.Transaction
import net.primal.domain.wallet.LnInvoiceCreateResult
import net.primal.domain.wallet.LnInvoiceParseResult
import net.primal.domain.wallet.LnUrlParseResult
import net.primal.domain.wallet.Network
import net.primal.domain.wallet.OnChainAddressResult
import net.primal.domain.wallet.SubWallet
import net.primal.domain.wallet.Wallet
import net.primal.domain.wallet.WalletPayParams
import net.primal.domain.wallet.WalletRepository
import net.primal.domain.wallet.WalletType
import net.primal.shared.data.local.db.withTransaction
import net.primal.wallet.data.handler.factory.HandlerFactory
import net.primal.wallet.data.local.dao.NostrWalletData
import net.primal.wallet.data.local.dao.WalletInfo
import net.primal.wallet.data.local.dao.WalletSettings
import net.primal.wallet.data.local.dao.WalletTransaction
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.model.CreateLightningInvoiceRequest
import net.primal.wallet.data.remote.api.PrimalWalletApi
import net.primal.wallet.data.remote.model.DepositRequestBody
import net.primal.wallet.data.repository.mappers.local.toDomain
import net.primal.wallet.data.repository.mappers.local.toWithdrawRequestDTO
import net.primal.wallet.data.repository.transactions.WalletTransactionsMediator
import net.primal.wallet.data.service.WalletService

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

    override suspend fun deleteWalletById(walletId: String) =
        withContext(dispatcherProvider.io()) {
            walletDatabase.wallet().deleteWalletById(walletId = walletId)
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
                        pubkey = wallet.pubkey,
                        walletPubkey = wallet.keypair.pubKey,
                        walletPrivateKey = wallet.keypair.privateKey,
                    ),
                )
            }
        }

    override fun latestTransactions(walletId: String): Flow<PagingData<Transaction>> {
        return createTransactionsPager(walletId) {
            walletDatabase.walletTransactions().latestTransactionsPagedByWalletId(walletId = walletId)
        }.flow.mapNotNull {
            it.map { txData ->
                when (txData.info.walletType) {
                    WalletType.PRIMAL -> {
                        val otherProfile = txData.primal?.otherUserId?.let { profileId ->
                            profileRepository.findProfileDataOrNull(profileId)
                        }
                        txData.toDomain(otherProfile = otherProfile)
                    }

                    WalletType.NWC -> txData.toDomain()
                }
            }
        }
    }

    override suspend fun findTransactionByIdOrNull(txId: String): Transaction? =
        withContext(dispatcherProvider.io()) {
            val transaction = walletDatabase.walletTransactions().findTransactionById(txId = txId)
                ?: return@withContext null

            val profile = transaction.primal?.otherUserId
                ?.let { profileRepository.findProfileDataOrNull(profileId = it) }

            transaction.toDomain(otherProfile = profile)
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
        walletId: String,
        amountInBtc: String?,
        comment: String?,
    ): Result<LnInvoiceCreateResult> {
        return withContext(dispatcherProvider.io()) {
            val wallet = walletDatabase.wallet().findWallet(walletId = walletId)
                ?: return@withContext Result.failure(
                    exception = IllegalArgumentException("Couldn't find wallet with the given walletId."),
                )

            when (wallet.info.type) {
                WalletType.PRIMAL -> primalWalletService.createLightningInvoice(
                    wallet = wallet.toDomain(),
                    request = CreateLightningInvoiceRequest.Primal(
                        description = comment,
                        subWallet = SubWallet.Open,
                        amountInBtc = amountInBtc,
                    ),
                )

                WalletType.NWC -> {
                    if (amountInBtc == null) {
                        return@withContext Result.failure(
                            exception = IllegalArgumentException("Amount is required for NWC invoices."),
                        )
                    }

                    nostrWalletService.createLightningInvoice(
                        wallet = wallet.toDomain(),
                        request = CreateLightningInvoiceRequest.NWC(
                            description = comment,
                            amountInMSats = amountInBtc.toDouble().btcToMSats().toLong(),
                            descriptionHash = null,
                            expiry = 1.hours.inWholeSeconds,
                        ),
                    )
                }
            }
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
        walletId: String,
        pagingSourceFactory: () -> PagingSource<Int, WalletTransaction>,
    ) = Pager(
        config = PagingConfig(
            pageSize = 50,
            prefetchDistance = 100,
            initialLoadSize = 200,
            enablePlaceholders = true,
        ),
        remoteMediator = WalletTransactionsMediator(
            walletId = walletId,
            dispatcherProvider = dispatcherProvider,
            transactionsHandler = HandlerFactory.createTransactionsHandler(
                dispatchers = dispatcherProvider,
                primalWalletService = primalWalletService,
                nostrWalletService = nostrWalletService,
                walletDatabase = walletDatabase,
                profileRepository = profileRepository,
            ),
            walletDatabase = walletDatabase,
        ),
        pagingSourceFactory = pagingSourceFactory,
    )
}
