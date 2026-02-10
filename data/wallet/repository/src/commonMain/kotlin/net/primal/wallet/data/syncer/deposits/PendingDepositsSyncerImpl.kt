package net.primal.wallet.data.syncer.deposits

import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.wallet.SparkWalletManager
import net.primal.domain.wallet.TxState
import net.primal.domain.wallet.TxType
import net.primal.domain.wallet.UnclaimedDepositEvent
import net.primal.domain.wallet.WalletType
import net.primal.domain.wallet.capabilities
import net.primal.domain.wallet.distinctUntilWalletIdChanged
import net.primal.domain.wallet.sync.PendingDepositsSyncer
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.TxKind
import net.primal.wallet.data.local.dao.ReceiveRequestType
import net.primal.wallet.data.local.dao.WalletTransactionData
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.service.MempoolApiService
import net.primal.wallet.data.service.totalReceivedSats

internal class PendingDepositsSyncerImpl(
    dispatcherProvider: DispatcherProvider,
    private val userId: String,
    private val walletDatabase: WalletDatabase,
    private val walletAccountRepository: WalletAccountRepository,
    private val sparkWalletManager: SparkWalletManager,
    private val mempoolApiService: MempoolApiService = MempoolApiService(),
) : PendingDepositsSyncer {

    private val scope = CoroutineScope(dispatcherProvider.io() + SupervisorJob())

    private var activeWalletObserverJob: Job? = null
    private var pollingJob: Job? = null
    private var unclaimedDepositsJob: Job? = null

    override fun start() {
        unclaimedDepositsJob?.cancel()
        unclaimedDepositsJob = scope.launch {
            sparkWalletManager.unclaimedDeposits.collect { event ->
                handleUnclaimedDeposits(event)
            }
        }

        activeWalletObserverJob?.cancel()
        activeWalletObserverJob = scope.launch {
            walletAccountRepository.observeActiveWallet(userId)
                .distinctUntilWalletIdChanged()
                .collect { wallet ->
                    pollingJob?.cancel()
                    if (wallet != null && wallet.capabilities.supportsReceivableTracking) {
                        pollingJob = scope.launch {
                            pollLoop(walletId = wallet.walletId, userId = userId)
                        }
                    }
                }
        }
    }

    override fun stop() {
        pollingJob?.cancel()
        activeWalletObserverJob?.cancel()
        unclaimedDepositsJob?.cancel()
    }

    private suspend fun handleUnclaimedDeposits(event: UnclaimedDepositEvent) {
        val userId = walletDatabase.wallet().findWalletInfo(event.walletId)?.userId ?: run {
            Napier.w(tag = TAG) { "Cannot resolve userId for walletId=${event.walletId}, skipping unclaimed deposits" }
            return
        }

        val now = Clock.System.now().epochSeconds

        val transactionRows = event.deposits.map { deposit ->
            WalletTransactionData(
                transactionId = deposit.txid,
                walletId = event.walletId,
                walletType = WalletType.SPARK,
                type = TxType.DEPOSIT,
                state = TxState.PROCESSING,
                createdAt = now,
                updatedAt = now,
                completedAt = null,
                userId = userId,
                note = null,
                invoice = null,
                amountInBtc = deposit.amountSats.toBtc().asEncryptable(),
                totalFeeInBtc = null,
                otherUserId = null,
                zappedEntity = null,
                zappedByUserId = null,
                txKind = TxKind.ON_CHAIN,
                onChainAddress = null,
                onChainTxId = deposit.txid.asEncryptable(),
                preimage = null,
                paymentHash = null,
                amountInUsd = null,
                exchangeRate = null,
                otherLightningAddress = null,
            )
        }

        if (transactionRows.isNotEmpty()) {
            walletDatabase.walletTransactions().upsertAll(data = transactionRows)
            walletDatabase.wallet().touchLastUpdatedAt(event.walletId)
        }
    }

    private suspend fun pollLoop(walletId: String, userId: String) {
        while (scope.isActive) {
            try {
                pollOnce(walletId = walletId, userId = userId)
            } catch (e: Exception) {
                Napier.w(tag = TAG) { "Mempool poll failed: ${e.message}" }
            }
            delay(POLL_INTERVAL)
        }
    }

    private suspend fun pollOnce(walletId: String, userId: String) {
        val createdAfter = Clock.System.now().epochSeconds - ADDRESS_TTL.inWholeSeconds
        val unfulfilledRequests = walletDatabase.receiveRequests().findUnfulfilled(
            walletId = walletId,
            type = ReceiveRequestType.ON_CHAIN,
            createdAfter = createdAfter,
            limit = MAX_ADDRESSES_TO_POLL,
        )

        if (unfulfilledRequests.isEmpty()) return

        Napier.d(tag = TAG) { "Polling ${unfulfilledRequests.size} unfulfilled address(es)" }

        val now = Clock.System.now().epochSeconds
        var depositsDetected = false

        for (request in unfulfilledRequests) {
            val address = request.payload
            val mempoolTxs = mempoolApiService.getUnconfirmedTransactions(address)

            Napier.d(tag = TAG) {
                "Address ${address.take(10)}...: ${mempoolTxs.size} mempool tx(s)"
            }

            for (tx in mempoolTxs) {
                val amountSats = tx.totalReceivedSats(address)
                if (amountSats <= 0) continue

                Napier.i(tag = TAG) {
                    "Mempool deposit detected: txid=${tx.txid}, amount=$amountSats sats, address=$address"
                }

                walletDatabase.walletTransactions().upsertAll(
                    data = listOf(
                        WalletTransactionData(
                            transactionId = tx.txid,
                            walletId = walletId,
                            walletType = WalletType.SPARK,
                            type = TxType.DEPOSIT,
                            state = TxState.CREATED,
                            createdAt = now,
                            updatedAt = now,
                            completedAt = null,
                            userId = userId,
                            note = null,
                            invoice = null,
                            amountInBtc = amountSats.toBtc().asEncryptable(),
                            totalFeeInBtc = null,
                            otherUserId = null,
                            zappedEntity = null,
                            zappedByUserId = null,
                            txKind = TxKind.ON_CHAIN,
                            onChainAddress = address.asEncryptable(),
                            onChainTxId = tx.txid.asEncryptable(),
                            preimage = null,
                            paymentHash = null,
                            amountInUsd = null,
                            exchangeRate = null,
                            otherLightningAddress = null,
                        ),
                    ),
                )

                Napier.i(tag = TAG) {
                    "Marking address as fulfilled: requestId=${request.id}, address=${address.take(10)}..."
                }
                walletDatabase.receiveRequests().markFulfilled(
                    id = request.id,
                    fulfilledAt = now,
                )

                depositsDetected = true
            }
        }

        if (depositsDetected) {
            walletDatabase.wallet().touchLastUpdatedAt(walletId)
        }
    }

    private companion object {
        private const val TAG = "PendingDepositsSyncer"
        private val POLL_INTERVAL = 15.seconds
        private val ADDRESS_TTL = 24.hours
        private const val MAX_ADDRESSES_TO_POLL = 5
    }
}
