package net.primal.wallet.data.syncer.deposits

import io.github.aakira.napier.Napier
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
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
import net.primal.wallet.data.service.MempoolAddressEvent
import net.primal.wallet.data.service.MempoolApiService
import net.primal.wallet.data.service.MempoolTransaction
import net.primal.wallet.data.service.MempoolWebSocketClient
import net.primal.wallet.data.service.TransactionLookupResult
import net.primal.wallet.data.service.totalReceivedSats

internal class PendingDepositsSyncerImpl(
    dispatcherProvider: DispatcherProvider,
    private val userId: String,
    private val walletDatabase: WalletDatabase,
    private val walletAccountRepository: WalletAccountRepository,
    private val sparkWalletManager: SparkWalletManager,
    private val mempoolApiService: MempoolApiService = MempoolApiService(),
    private val mempoolWebSocketClient: MempoolWebSocketClient = MempoolWebSocketClient(),
) : PendingDepositsSyncer {

    private val scope = CoroutineScope(dispatcherProvider.io() + SupervisorJob())

    private var activeWalletObserverJob: Job? = null
    private var mempoolMonitorJob: Job? = null
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
                    mempoolMonitorJob?.cancel()
                    if (wallet != null && wallet.capabilities.supportsReceivableTracking) {
                        mempoolMonitorJob = scope.launch {
                            initialFetch(walletId = wallet.walletId, userId = userId)
                            cleanupStalePendingTransactions(walletId = wallet.walletId)
                            webSocketLoop(walletId = wallet.walletId, userId = userId)
                        }
                    }
                }
        }
    }

    override fun stop() {
        mempoolMonitorJob?.cancel()
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

    private suspend fun initialFetch(walletId: String, userId: String) {
        val createdAfter = Clock.System.now().epochSeconds - ADDRESS_TTL.inWholeSeconds
        val allRequests = walletDatabase.receiveRequests().findAll(
            walletId = walletId,
            type = ReceiveRequestType.ON_CHAIN,
            createdAfter = createdAfter,
            limit = MAX_ADDRESSES_TO_FETCH,
        )

        if (allRequests.isEmpty()) return

        Napier.d(tag = TAG) { "Initial fetch: checking ${allRequests.size} address(es)" }

        val now = Clock.System.now().epochSeconds
        var depositsDetected = false

        for (request in allRequests) {
            val address = request.payload
            try {
                val mempoolTxs = mempoolApiService.getUnconfirmedTransactions(address)

                Napier.d(tag = TAG) {
                    "Address ${address.take(10)}...: ${mempoolTxs.size} mempool tx(s)"
                }

                for (tx in mempoolTxs) {
                    if (processMempoolDeposit(tx, address, walletId, userId, now, request.id)) {
                        depositsDetected = true
                    }
                }
            } catch (e: Exception) {
                Napier.w(throwable = e, tag = TAG) { "Initial fetch failed for ${address.take(10)}...: ${e.message}" }
            }
        }

        if (depositsDetected) {
            walletDatabase.wallet().touchLastUpdatedAt(walletId)
        }
    }

    private suspend fun cleanupStalePendingTransactions(walletId: String) {
        val createdTxs = walletDatabase.walletTransactions().findCreatedOnChain(walletId)

        for (tx in createdTxs) {
            try {
                val result = mempoolApiService.lookupTransaction(tx.transactionId)
                if (result is TransactionLookupResult.NotFound) {
                    Napier.i(tag = TAG) { "Removing stale tx: ${tx.transactionId}" }
                    walletDatabase.walletTransactions().deleteByTransactionId(tx.transactionId)
                }
            } catch (e: Exception) {
                Napier.w(throwable = e, tag = TAG) { "Failed to verify tx ${tx.transactionId}: ${e.message}" }
            }
        }
    }

    private suspend fun webSocketLoop(walletId: String, userId: String) {
        var reconnectDelay = RECONNECT_DELAY_INITIAL
        while (scope.isActive) {
            val address = resolveNewestAddress(walletId)
            if (address == null) {
                delay(ADDRESS_REFRESH_INTERVAL)
                continue
            }

            Napier.d(tag = TAG) { "WebSocket subscribing to ${address.take(10)}..." }

            try {
                mempoolWebSocketClient.observeAddress(address).collect { event ->
                    reconnectDelay = RECONNECT_DELAY_INITIAL
                    handleMempoolEvent(event, walletId, userId, address)
                }
            } catch (e: Exception) {
                Napier.w(throwable = e, tag = TAG) { "WebSocket disconnected: ${e.message}" }
            }

            delay(reconnectDelay)
            reconnectDelay = (reconnectDelay * 2).coerceAtMost(RECONNECT_DELAY_MAX)
        }
    }

    private suspend fun resolveNewestAddress(walletId: String): String? {
        val createdAfter = Clock.System.now().epochSeconds - ADDRESS_TTL.inWholeSeconds
        val requests = walletDatabase.receiveRequests().findAll(
            walletId = walletId,
            type = ReceiveRequestType.ON_CHAIN,
            createdAfter = createdAfter,
            limit = 1,
        )
        return requests.firstOrNull()?.payload
    }

    private suspend fun handleMempoolEvent(
        event: MempoolAddressEvent,
        walletId: String,
        userId: String,
        address: String,
    ) {
        val transactions = when (event) {
            is MempoolAddressEvent.MempoolTx -> event.transactions
            is MempoolAddressEvent.ConfirmedTx -> event.transactions
        }

        val now = Clock.System.now().epochSeconds
        var depositsDetected = false

        for (tx in transactions) {
            if (processMempoolDeposit(tx, address, walletId, userId, now)) {
                depositsDetected = true
            }
        }

        if (depositsDetected) {
            walletDatabase.wallet().touchLastUpdatedAt(walletId)
        }
    }

    private suspend fun processMempoolDeposit(
        tx: MempoolTransaction,
        address: String,
        walletId: String,
        userId: String,
        now: Long,
        requestId: Long? = null,
    ): Boolean {
        val amountSats = tx.totalReceivedSats(address)
        if (amountSats <= 0) return false

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

        if (requestId != null) {
            walletDatabase.receiveRequests().markFulfilled(id = requestId, fulfilledAt = now)
        }

        return true
    }

    private companion object {
        private const val TAG = "PendingDepositsSyncer"
        private val ADDRESS_TTL = 7.days
        private const val MAX_ADDRESSES_TO_FETCH = 5
        private val RECONNECT_DELAY_INITIAL = 5.seconds
        private val RECONNECT_DELAY_MAX = 5.minutes
        private val ADDRESS_REFRESH_INTERVAL = 60.seconds
    }
}
