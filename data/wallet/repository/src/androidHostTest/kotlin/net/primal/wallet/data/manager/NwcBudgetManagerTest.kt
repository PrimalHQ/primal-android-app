package net.primal.wallet.data.manager

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.util.UUID
import kotlin.time.Clock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.connections.nostr.model.BudgetReservationResult
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.dao.NostrWalletConnectionData
import net.primal.wallet.data.local.dao.NwcBudgetDao
import net.primal.wallet.data.local.dao.NwcBudgetReservationData
import net.primal.wallet.data.local.dao.ReservationStatus
import net.primal.wallet.data.local.db.WalletDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class NwcBudgetManagerTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dispatcherProvider = object : DispatcherProvider {
        override fun io() = testDispatcher
        override fun main() = testDispatcher
    }

    private lateinit var database: WalletDatabase
    private lateinit var budgetDao: NwcBudgetDao
    private lateinit var budgetManager: NwcBudgetManager

    @Before
    fun setUp() {
        WalletDatabase.setEncryption(enableEncryption = false)
        val context = ApplicationProvider.getApplicationContext<Context>()
        val dbName = "wallet_budget_test_${UUID.randomUUID()}.db"
        database = Room.databaseBuilder<WalletDatabase>(
            context = context,
            name = dbName,
        )
            .setDriver(AndroidSQLiteDriver())
            .allowMainThreadQueries()
            .build()

        budgetDao = database.nwcBudget()
        budgetManager = NwcBudgetManager(
            dispatcherProvider = dispatcherProvider,
            walletDatabase = database,
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun reserveBudgetReturnsUnlimitedWhenBudgetMissing() =
        runTest {
            val connectionId = "connection-missing"
            database.nwcConnections().upsert(
                buildConnectionData(
                    secretPubKey = connectionId,
                    dailyBudgetSats = null,
                ),
            )

            val result = budgetManager.reserveBudget(
                connectionId = connectionId,
                amountSats = 100,
                requestId = "request-1",
                timeoutMs = 1_000,
            )

            result.shouldBeInstanceOf<BudgetReservationResult.Unlimited>()
        }

    @Test
    fun reserveBudgetCreatesPendingReservation() =
        runTest {
            val connectionId = "connection-1"
            database.nwcConnections().upsert(buildConnectionData(secretPubKey = connectionId))

            val result = budgetManager.reserveBudget(
                connectionId = connectionId,
                amountSats = 200,
                requestId = "request-1",
                timeoutMs = 5_000,
            )

            val reserved = result.shouldBeInstanceOf<BudgetReservationResult.Reserved>()
            reserved.amountSats shouldBe 200L
            reserved.remainingBudget shouldBe 800L

            val reservation = budgetDao.getReservationById(reserved.reservationId).shouldNotBeNull()
            reservation.status shouldBe ReservationStatus.PENDING
            reservation.amountSats.decrypted shouldBe 200L
        }

    @Test
    fun reserveBudgetConsidersPendingReservations() =
        runTest {
            val connectionId = "connection-2"
            database.nwcConnections().upsert(buildConnectionData(secretPubKey = connectionId))

            budgetManager.reserveBudget(
                connectionId = connectionId,
                amountSats = 200,
                requestId = "request-1",
                timeoutMs = 5_000,
            )

            val result = budgetManager.reserveBudget(
                connectionId = connectionId,
                amountSats = 900,
                requestId = "request-2",
                timeoutMs = 5_000,
            )

            val insufficient = result.shouldBeInstanceOf<BudgetReservationResult.InsufficientBudget>()
            insufficient.requested shouldBe 900L
            insufficient.available shouldBe 800L
        }

    @Test
    fun commitReservationUpdatesSpendWithReservedAmount() =
        runTest {
            val connectionId = "connection-3"
            database.nwcConnections().upsert(buildConnectionData(secretPubKey = connectionId))

            val result = budgetManager.reserveBudget(
                connectionId = connectionId,
                amountSats = 250,
                requestId = "request-1",
                timeoutMs = 5_000,
            )

            val reservationId = result.shouldBeInstanceOf<BudgetReservationResult.Reserved>().reservationId
            budgetManager.commitReservation(reservationId = reservationId, actualAmountSats = null)

            val reservation = budgetDao.getReservationById(reservationId).shouldNotBeNull()
            reservation.status shouldBe ReservationStatus.COMMITTED

            val dailySpend = budgetDao.getDailySpend(
                connectionId = connectionId,
                budgetDate = reservation.budgetDate,
            )
            dailySpend?.confirmedSpendSats?.decrypted shouldBe 250L
        }

    @Test
    fun commitReservationUsesActualAmountWhenProvided() =
        runTest {
            val connectionId = "connection-4"
            database.nwcConnections().upsert(buildConnectionData(secretPubKey = connectionId))

            val result = budgetManager.reserveBudget(
                connectionId = connectionId,
                amountSats = 150,
                requestId = "request-1",
                timeoutMs = 5_000,
            )

            val reservationId = result.shouldBeInstanceOf<BudgetReservationResult.Reserved>().reservationId
            budgetManager.commitReservation(reservationId = reservationId, actualAmountSats = 175)

            val reservation = budgetDao.getReservationById(reservationId).shouldNotBeNull()
            reservation.status shouldBe ReservationStatus.COMMITTED

            val dailySpend = budgetDao.getDailySpend(
                connectionId = connectionId,
                budgetDate = reservation.budgetDate,
            )
            dailySpend?.confirmedSpendSats?.decrypted shouldBe 175L
        }

    @Test
    fun releaseReservationMarksReservationReleased() =
        runTest {
            val connectionId = "connection-5"
            database.nwcConnections().upsert(buildConnectionData(secretPubKey = connectionId))

            val result = budgetManager.reserveBudget(
                connectionId = connectionId,
                amountSats = 120,
                requestId = "request-1",
                timeoutMs = 5_000,
            )

            val reservationId = result.shouldBeInstanceOf<BudgetReservationResult.Reserved>().reservationId
            budgetManager.releaseReservation(reservationId = reservationId)

            val reservation = budgetDao.getReservationById(reservationId).shouldNotBeNull()
            reservation.status shouldBe ReservationStatus.RELEASED

            val dailySpend = budgetDao.getDailySpend(
                connectionId = connectionId,
                budgetDate = reservation.budgetDate,
            )
            dailySpend shouldBe null
        }

    @Test
    fun commitAfterReleaseDoesNotChangeSpend() =
        runTest {
            val connectionId = "connection-6"
            database.nwcConnections().upsert(buildConnectionData(secretPubKey = connectionId))

            val result = budgetManager.reserveBudget(
                connectionId = connectionId,
                amountSats = 200,
                requestId = "request-1",
                timeoutMs = 5_000,
            )

            val reservationId = result.shouldBeInstanceOf<BudgetReservationResult.Reserved>().reservationId
            budgetManager.releaseReservation(reservationId = reservationId)
            budgetManager.commitReservation(reservationId = reservationId, actualAmountSats = 300)

            val reservation = budgetDao.getReservationById(reservationId).shouldNotBeNull()
            reservation.status shouldBe ReservationStatus.RELEASED

            val dailySpend = budgetDao.getDailySpend(
                connectionId = connectionId,
                budgetDate = reservation.budgetDate,
            )
            dailySpend shouldBe null
        }

    @Test
    fun cleanupExpiredReservationsExpiresOnlyStaleEntries() =
        runTest {
            val connectionId = "connection-7"
            database.nwcConnections().upsert(buildConnectionData(secretPubKey = connectionId))

            val now = Clock.System.now().toEpochMilliseconds()
            val budgetDate = currentBudgetDate()

            val expiredPending = insertReservation(
                connectionId = connectionId,
                requestId = "request-expired-pending",
                status = ReservationStatus.PENDING,
                budgetDate = budgetDate,
                expiresAt = now - 1,
            )
            val expiredProcessing = insertReservation(
                connectionId = connectionId,
                requestId = "request-expired-processing",
                status = ReservationStatus.PROCESSING,
                budgetDate = budgetDate,
                expiresAt = now - 1,
            )
            val activePending = insertReservation(
                connectionId = connectionId,
                requestId = "request-active",
                status = ReservationStatus.PENDING,
                budgetDate = budgetDate,
                expiresAt = now + 60_000,
            )

            budgetManager.cleanupExpiredReservations()

            budgetDao.getReservationById(expiredPending).shouldNotBeNull().status shouldBe ReservationStatus.EXPIRED
            budgetDao.getReservationById(expiredProcessing).shouldNotBeNull().status shouldBe ReservationStatus.EXPIRED
            budgetDao.getReservationById(activePending).shouldNotBeNull().status shouldBe ReservationStatus.PENDING
        }

    private fun buildConnectionData(secretPubKey: String, dailyBudgetSats: Long? = 1_000L) =
        NostrWalletConnectionData(
            secretPubKey = secretPubKey,
            walletId = "wallet-$secretPubKey",
            userId = "user-$secretPubKey",
            servicePubKey = "service-pub-$secretPubKey",
            servicePrivateKey = "service-priv-$secretPubKey".asEncryptable(),
            relay = "wss://relay.example/$secretPubKey".asEncryptable(),
            appName = "Test App".asEncryptable(),
            dailyBudgetSats = dailyBudgetSats?.asEncryptable(),
        )

    private suspend fun insertReservation(
        connectionId: String,
        requestId: String,
        status: ReservationStatus,
        budgetDate: String,
        expiresAt: Long,
    ): String {
        val now = Clock.System.now().toEpochMilliseconds()
        val reservationId = UUID.randomUUID().toString()
        budgetDao.insertReservation(
            NwcBudgetReservationData(
                reservationId = reservationId,
                connectionId = connectionId,
                requestId = requestId,
                amountSats = 100L.asEncryptable(),
                status = status,
                budgetDate = budgetDate,
                createdAt = now,
                expiresAt = expiresAt,
                updatedAt = now,
            ),
        )
        return reservationId
    }

    private fun currentBudgetDate(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.UTC).date.toString()
    }
}
