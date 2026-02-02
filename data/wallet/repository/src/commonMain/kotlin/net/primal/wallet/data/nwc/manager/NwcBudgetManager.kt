package net.primal.wallet.data.nwc.manager

import kotlin.time.Clock
import kotlin.uuid.Uuid
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.connections.nostr.model.NwcPaymentHoldResult
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.dao.nwc.NwcDailyBudgetData
import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldDao
import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldData
import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldStatus
import net.primal.wallet.data.local.db.WalletDatabase

/**
 * Manages daily budget enforcement for NWC connections using payment holds.
 *
 * ## Usage Flow
 * ```
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  1. External app sends pay_invoice request                      │
 * │                        ↓                                        │
 * │  2. Check if hold is required: hasBudgetLimit(connectionId)     │
 * │                        ↓                                        │
 * │     ┌─────────────────┴─────────────────┐                       │
 * │     │                                   │                       │
 * │  false (no limit)                    true (has limit)           │
 * │     │                                   │                       │
 * │     ↓                                   ↓                       │
 * │  Execute payment          3. placeHold(connectionId, amount)    │
 * │                                         │                       │
 * │                              ┌──────────┴──────────┐            │
 * │                              │                     │            │
 * │                           Placed           InsufficientBudget   │
 * │                              │                     │            │
 * │                              ↓                     ↓            │
 * │                    4. Execute payment        Reject request     │
 * │                              │              (QUOTA_EXCEEDED)    │
 * │                   ┌──────────┴──────────┐                       │
 * │                SUCCESS               FAILURE                    │
 * │                   │                     │                       │
 * │                   ↓                     ↓                       │
 * │            commitHold()          releaseHold()                  │
 * │                   │                     │                       │
 * │                   ↓                     ↓                       │
 * │            - status=COMMITTED    - status=RELEASED              │
 * │            - Update daily spend  - Budget freed                 │
 * └─────────────────────────────────────────────────────────────────┘
 * ```
 *
 * @see NwcPaymentHoldResult
 * @see NwcPaymentHoldStatus
 */
internal class NwcBudgetManager(
    private val dispatcherProvider: DispatcherProvider,
    private val walletDatabase: WalletDatabase,
) {
    private val locksGuard = Mutex()
    private val connectionLocks = mutableMapOf<String, Mutex>()

    private val paymentHoldDao: NwcPaymentHoldDao
        get() = walletDatabase.nwcPaymentHolds()

    private suspend fun acquireMutex(connectionId: String): Mutex {
        return locksGuard.withLock {
            connectionLocks.getOrPut(connectionId) { Mutex() }
        }
    }

    /**
     * Checks if the connection has a daily budget limit configured.
     * If true, caller should use [placeHold] before processing payments.
     * If false, payments can proceed without budget holds.
     */
    suspend fun hasBudgetLimit(connectionId: String): Boolean =
        withContext(dispatcherProvider.io()) {
            walletDatabase.nwcConnections()
                .findConnection(connectionId)?.dailyBudgetSats != null
        }

    suspend fun getAvailableBudgetSats(connectionId: String): Long? =
        withContext(dispatcherProvider.io()) {
            val dailyBudgetSats = walletDatabase.nwcConnections()
                .findConnection(connectionId)?.dailyBudgetSats?.decrypted
                ?: return@withContext null

            val today = getCurrentBudgetDate()
            val confirmedSpend = getConfirmedSpend(connectionId, today)
            val pendingHolds = getPendingHoldsSum(connectionId, today)

            (dailyBudgetSats - confirmedSpend - pendingHolds).coerceAtLeast(0L)
        }

    /**
     * Places a hold on the daily budget for the given amount.
     *
     * @throws IllegalStateException if no budget limit is configured for this connection.
     *         Caller should check [hasBudgetLimit] first.
     */
    suspend fun placeHold(
        connectionId: String,
        amountSats: Long,
        requestId: String,
        timeoutMs: Long,
    ): NwcPaymentHoldResult =
        withContext(dispatcherProvider.io()) {
            acquireMutex(connectionId).withLock {
                walletDatabase.withTransaction {
                    val dailyBudgetSats = walletDatabase.nwcConnections()
                        .findConnection(connectionId)?.dailyBudgetSats?.decrypted
                        ?: error("No budget limit configured for connection: $connectionId")

                    val today = getCurrentBudgetDate()

                    val confirmedSpend = getConfirmedSpend(connectionId, today)
                    val pendingHolds = getPendingHoldsSum(connectionId, today)
                    val available = dailyBudgetSats - confirmedSpend - pendingHolds

                    if (amountSats > available) {
                        return@withTransaction NwcPaymentHoldResult.InsufficientBudget(
                            requested = amountSats,
                            available = available,
                        )
                    }

                    val now = Clock.System.now().toEpochMilliseconds()

                    val holdId = Uuid.random().toString()

                    paymentHoldDao.insertHold(
                        NwcPaymentHoldData(
                            holdId = holdId,
                            connectionId = connectionId,
                            requestId = requestId,
                            amountSats = amountSats.asEncryptable(),
                            status = NwcPaymentHoldStatus.PENDING,
                            budgetDate = today,
                            createdAt = now,
                            expiresAt = now + timeoutMs,
                            updatedAt = now,
                        ),
                    )

                    NwcPaymentHoldResult.Placed(
                        holdId = holdId,
                        amountSats = amountSats,
                        remainingBudget = available - amountSats,
                    )
                }
            }
        }

    suspend fun commitHold(holdId: String, actualAmountSats: Long?) =
        withContext(dispatcherProvider.io()) {
            val hold = paymentHoldDao.getHoldById(holdId) ?: return@withContext
            acquireMutex(hold.connectionId).withLock {
                walletDatabase.withTransaction {
                    val currentHold = paymentHoldDao.getHoldById(holdId) ?: return@withTransaction

                    if (currentHold.status != NwcPaymentHoldStatus.PENDING &&
                        currentHold.status != NwcPaymentHoldStatus.PROCESSING
                    ) {
                        return@withTransaction
                    }

                    val now = Clock.System.now().toEpochMilliseconds()
                    val amountToAdd = actualAmountSats ?: currentHold.amountSats.decrypted

                    paymentHoldDao.updateHold(
                        currentHold.copy(
                            status = NwcPaymentHoldStatus.COMMITTED,
                            updatedAt = now,
                        ),
                    )

                    val currentSpend = paymentHoldDao.getDailyBudget(
                        connectionId = currentHold.connectionId,
                        budgetDate = currentHold.budgetDate,
                    )

                    val newConfirmedSpend = (currentSpend?.confirmedSpendSats?.decrypted ?: 0L) + amountToAdd

                    paymentHoldDao.upsertDailyBudget(
                        NwcDailyBudgetData(
                            connectionId = currentHold.connectionId,
                            budgetDate = currentHold.budgetDate,
                            confirmedSpendSats = newConfirmedSpend.asEncryptable(),
                            lastUpdatedAt = now,
                        ),
                    )
                }
            }
        }

    suspend fun releaseHold(holdId: String) =
        withContext(dispatcherProvider.io()) {
            val hold = paymentHoldDao.getHoldById(holdId) ?: return@withContext
            acquireMutex(hold.connectionId).withLock {
                walletDatabase.withTransaction {
                    val currentHold = paymentHoldDao.getHoldById(holdId) ?: return@withTransaction

                    if (currentHold.status != NwcPaymentHoldStatus.PENDING &&
                        currentHold.status != NwcPaymentHoldStatus.PROCESSING
                    ) {
                        return@withTransaction
                    }

                    val now = Clock.System.now().toEpochMilliseconds()
                    paymentHoldDao.updateHold(
                        currentHold.copy(
                            status = NwcPaymentHoldStatus.RELEASED,
                            updatedAt = now,
                        ),
                    )
                }
            }
        }

    suspend fun cleanupExpiredHolds() =
        withContext(dispatcherProvider.io()) {
            val now = Clock.System.now().toEpochMilliseconds()
            paymentHoldDao.expireStaleHolds(now = now, updatedAt = now)
        }

    private suspend fun getConfirmedSpend(connectionId: String, budgetDate: String): Long {
        return paymentHoldDao.getDailyBudget(connectionId, budgetDate)
            ?.confirmedSpendSats?.decrypted
            ?: 0L
    }

    private suspend fun getPendingHoldsSum(connectionId: String, budgetDate: String): Long {
        return paymentHoldDao.getPendingHolds(connectionId, budgetDate)
            .sumOf { it.amountSats.decrypted }
    }

    private fun getCurrentBudgetDate(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.UTC).date.toString()
    }
}
