package net.primal.wallet.data.manager

import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.connections.nostr.model.BudgetReservationResult
import net.primal.shared.data.local.db.withTransaction
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.dao.NwcBudgetDao
import net.primal.wallet.data.local.dao.NwcBudgetReservationData
import net.primal.wallet.data.local.dao.NwcDailySpendData
import net.primal.wallet.data.local.dao.ReservationStatus
import net.primal.wallet.data.local.db.WalletDatabase

@OptIn(ExperimentalUuidApi::class)
internal class NwcBudgetManager(
    private val dispatcherProvider: DispatcherProvider,
    private val walletDatabase: WalletDatabase,
) {
    private val connectionLocks = mutableMapOf<String, Mutex>()
    private val locksGuard = Mutex()

    private val budgetDao: NwcBudgetDao
        get() = walletDatabase.nwcBudget()

    private suspend fun getMutexForConnection(connectionId: String): Mutex {
        return locksGuard.withLock {
            connectionLocks.getOrPut(connectionId) { Mutex() }
        }
    }

    /**                     USAGE EXPLANATION
     *  ┌─────────────────────────────────────────────────────────────────┐
     *   │  1. External app sends pay_invoice request                      │
     *   │                        ↓                                        │
     *   │  2. NwcBudgetManager.reserveBudget(connectionId, amount)        │
     *   │                        ↓                                        │
     *   │  3. Per-connection mutex acquired (different connections        │
     *   │     can process in parallel)                                    │
     *   │                        ↓                                        │
     *   │  4. Room transaction:                                           │
     *   │     - Get connection config (dailyBudgetSats, budgetResetHour)  │
     *   │     - Calculate today's budget date                             │
     *   │     - Get confirmed spend from NwcDailySpendData                │
     *   │     - Get pending reservations sum                              │
     *   │     - available = dailyBudget - confirmed - pending             │
     *   │                        ↓                                        │
     *   │     ┌─────────────────┴─────────────────┐                      │
     *   │     │                                   │                      │
     *   │  amount > available              amount <= available           │
     *   │     │                                   │                      │
     *   │     ↓                                   ↓                      │
     *   │  InsufficientBudget              Create reservation            │
     *   │  (QUOTA_EXCEEDED)                (status=PENDING)              │
     *   │                                         │                      │
     *   │                                         ↓                      │
     *   │                              5. Execute payment                │
     *   │                                         │                      │
     *   │                          ┌──────────────┴──────────────┐       │
     *   │                       SUCCESS                       FAILURE    │
     *   │                          │                              │      │
     *   │                          ↓                              ↓      │
     *   │                   commitReservation()          releaseReservation()
     *   │                          │                              │      │
     *   │                          ↓                              ↓      │
     *   │                   - status=COMMITTED            - status=RELEASED
     *   │                   - Add to NwcDailySpendData    - Budget freed │
     *   └─────────────────────────────────────────────────────────────────┘
     */

    suspend fun reserveBudget(
        connectionId: String,
        amountSats: Long,
        requestId: String,
        timeoutMs: Long,
    ): BudgetReservationResult =
        withContext(dispatcherProvider.io()) {
            val mutex = getMutexForConnection(connectionId)

            mutex.withLock {
                walletDatabase.withTransaction {
                    val dailyBudgetSats = walletDatabase.nwcConnections()
                        .findConnection(connectionId)?.dailyBudgetSats?.decrypted
                        ?: return@withTransaction BudgetReservationResult.Unlimited

                    val today = getCurrentBudgetDate()

                    val confirmedSpend = getConfirmedSpend(connectionId, today)
                    val pendingReservations = getPendingReservationsSum(connectionId, today)
                    val available = dailyBudgetSats - confirmedSpend - pendingReservations

                    if (amountSats > available) {
                        return@withTransaction BudgetReservationResult.InsufficientBudget(
                            requested = amountSats,
                            available = available,
                        )
                    }

                    val now = Clock.System.now().toEpochMilliseconds()

                    val reservationId = Uuid.random().toString()

                    budgetDao.insertReservation(
                        NwcBudgetReservationData(
                            reservationId = reservationId,
                            connectionId = connectionId,
                            requestId = requestId,
                            amountSats = amountSats.asEncryptable(),
                            status = ReservationStatus.PENDING,
                            budgetDate = today,
                            createdAt = now,
                            expiresAt = now + timeoutMs,
                            updatedAt = now,
                        ),
                    )

                    BudgetReservationResult.Reserved(
                        reservationId = reservationId,
                        amountSats = amountSats,
                        remainingBudget = available - amountSats,
                    )
                }
            }
        }

    suspend fun commitReservation(reservationId: String, actualAmountSats: Long?) =
        withContext(dispatcherProvider.io()) {
            val reservation = budgetDao.getReservationById(reservationId)
                ?: return@withContext
            val mutex = getMutexForConnection(reservation.connectionId)

            mutex.withLock {
                walletDatabase.withTransaction {
                    val currentReservation = budgetDao.getReservationById(reservationId)
                        ?: return@withTransaction

                    if (currentReservation.status != ReservationStatus.PENDING &&
                        currentReservation.status != ReservationStatus.PROCESSING
                    ) {
                        return@withTransaction
                    }

                    val now = Clock.System.now().toEpochMilliseconds()
                    val amountToAdd = actualAmountSats ?: currentReservation.amountSats.decrypted

                    budgetDao.updateReservation(
                        currentReservation.copy(
                            status = ReservationStatus.COMMITTED,
                            updatedAt = now,
                        ),
                    )

                    val currentSpend = budgetDao.getDailySpend(
                        connectionId = currentReservation.connectionId,
                        budgetDate = currentReservation.budgetDate,
                    )

                    val newConfirmedSpend =
                        (currentSpend?.confirmedSpendSats?.decrypted ?: 0L) + amountToAdd

                    budgetDao.upsertDailySpend(
                        NwcDailySpendData(
                            connectionId = currentReservation.connectionId,
                            budgetDate = currentReservation.budgetDate,
                            confirmedSpendSats = newConfirmedSpend.asEncryptable(),
                            lastUpdatedAt = now,
                        ),
                    )
                }
            }
        }

    suspend fun releaseReservation(reservationId: String) =
        withContext(dispatcherProvider.io()) {
            val reservation = budgetDao.getReservationById(reservationId)
                ?: return@withContext
            val mutex = getMutexForConnection(reservation.connectionId)

            mutex.withLock {
                walletDatabase.withTransaction {
                    val currentReservation = budgetDao.getReservationById(reservationId)
                        ?: return@withTransaction

                    if (currentReservation.status != ReservationStatus.PENDING &&
                        currentReservation.status != ReservationStatus.PROCESSING
                    ) {
                        return@withTransaction
                    }

                    val now = Clock.System.now().toEpochMilliseconds()
                    budgetDao.updateReservation(
                        currentReservation.copy(
                            status = ReservationStatus.RELEASED,
                            updatedAt = now,
                        ),
                    )
                }
            }
        }

    /* TODO: Something like this should be ran at times to free up reserved resources. */
    suspend fun cleanupExpiredReservations() =
        withContext(dispatcherProvider.io()) {
            val now = Clock.System.now().toEpochMilliseconds()
            budgetDao.expireStaleReservations(now = now, updatedAt = now)
        }

    private suspend fun getConfirmedSpend(connectionId: String, budgetDate: String): Long {
        return budgetDao.getDailySpend(connectionId, budgetDate)
            ?.confirmedSpendSats?.decrypted
            ?: 0L
    }

    private suspend fun getPendingReservationsSum(connectionId: String, budgetDate: String): Long {
        return budgetDao.getPendingReservations(connectionId, budgetDate)
            .sumOf { it.amountSats.decrypted }
    }

    private fun getCurrentBudgetDate(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.UTC).date.toString()
    }
}
