package net.primal.wallet.data.manager

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import io.kotest.assertions.throwables.shouldThrow
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
import net.primal.domain.connections.nostr.model.NwcPaymentHoldResult
import net.primal.shared.data.local.encryption.asEncryptable
import net.primal.wallet.data.local.dao.nwc.NwcConnectionData
import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldDao
import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldData
import net.primal.wallet.data.local.dao.nwc.NwcPaymentHoldStatus
import net.primal.wallet.data.local.db.WalletDatabase
import net.primal.wallet.data.nwc.manager.NwcBudgetManager
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
    private lateinit var paymentHoldDao: NwcPaymentHoldDao
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

        paymentHoldDao = database.nwcPaymentHolds()
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
    fun hasBudgetLimitReturnsFalseWhenNoBudgetConfigured() =
        runTest {
            val connectionId = "connection-no-budget"
            database.nwcConnections().upsert(
                buildConnectionData(
                    secretPubKey = connectionId,
                    dailyBudgetSats = null,
                ),
            )

            val result = budgetManager.hasBudgetLimit(connectionId)

            result shouldBe false
        }

    @Test
    fun hasBudgetLimitReturnsTrueWhenBudgetConfigured() =
        runTest {
            val connectionId = "connection-with-budget"
            database.nwcConnections().upsert(
                buildConnectionData(
                    secretPubKey = connectionId,
                    dailyBudgetSats = 1_000L,
                ),
            )

            val result = budgetManager.hasBudgetLimit(connectionId)

            result shouldBe true
        }

    @Test
    fun placeHoldThrowsWhenNoBudgetConfigured() =
        runTest {
            val connectionId = "connection-no-budget"
            database.nwcConnections().upsert(
                buildConnectionData(
                    secretPubKey = connectionId,
                    dailyBudgetSats = null,
                ),
            )

            shouldThrow<IllegalStateException> {
                budgetManager.placeHold(
                    connectionId = connectionId,
                    amountSats = 100,
                    requestId = "request-1",
                    timeoutMs = 1_000,
                )
            }
        }

    @Test
    fun placeHoldCreatesPendingHold() =
        runTest {
            val connectionId = "connection-1"
            database.nwcConnections().upsert(buildConnectionData(secretPubKey = connectionId))

            val result = budgetManager.placeHold(
                connectionId = connectionId,
                amountSats = 200,
                requestId = "request-1",
                timeoutMs = 5_000,
            )

            val placed = result.shouldBeInstanceOf<NwcPaymentHoldResult.Placed>()
            placed.amountSats shouldBe 200L
            placed.remainingBudget shouldBe 800L

            val hold = paymentHoldDao.getHoldById(placed.holdId).shouldNotBeNull()
            hold.status shouldBe NwcPaymentHoldStatus.PENDING
            hold.amountSats.decrypted shouldBe 200L
        }

    @Test
    fun placeHoldConsidersPendingHolds() =
        runTest {
            val connectionId = "connection-2"
            database.nwcConnections().upsert(buildConnectionData(secretPubKey = connectionId))

            budgetManager.placeHold(
                connectionId = connectionId,
                amountSats = 200,
                requestId = "request-1",
                timeoutMs = 5_000,
            )

            val result = budgetManager.placeHold(
                connectionId = connectionId,
                amountSats = 900,
                requestId = "request-2",
                timeoutMs = 5_000,
            )

            val insufficient = result.shouldBeInstanceOf<NwcPaymentHoldResult.InsufficientBudget>()
            insufficient.requested shouldBe 900L
            insufficient.available shouldBe 800L
        }

    @Test
    fun commitHoldUpdatesSpendWithHeldAmount() =
        runTest {
            val connectionId = "connection-3"
            database.nwcConnections().upsert(buildConnectionData(secretPubKey = connectionId))

            val result = budgetManager.placeHold(
                connectionId = connectionId,
                amountSats = 250,
                requestId = "request-1",
                timeoutMs = 5_000,
            )

            val holdId = result.shouldBeInstanceOf<NwcPaymentHoldResult.Placed>().holdId
            budgetManager.commitHold(holdId = holdId, actualAmountSats = null)

            val hold = paymentHoldDao.getHoldById(holdId).shouldNotBeNull()
            hold.status shouldBe NwcPaymentHoldStatus.COMMITTED

            val dailySpend = paymentHoldDao.getDailyBudget(
                connectionId = connectionId,
                budgetDate = hold.budgetDate,
            )
            dailySpend?.confirmedSpendSats?.decrypted shouldBe 250L
        }

    @Test
    fun commitHoldUsesActualAmountWhenProvided() =
        runTest {
            val connectionId = "connection-4"
            database.nwcConnections().upsert(buildConnectionData(secretPubKey = connectionId))

            val result = budgetManager.placeHold(
                connectionId = connectionId,
                amountSats = 150,
                requestId = "request-1",
                timeoutMs = 5_000,
            )

            val holdId = result.shouldBeInstanceOf<NwcPaymentHoldResult.Placed>().holdId
            budgetManager.commitHold(holdId = holdId, actualAmountSats = 175)

            val hold = paymentHoldDao.getHoldById(holdId).shouldNotBeNull()
            hold.status shouldBe NwcPaymentHoldStatus.COMMITTED

            val dailySpend = paymentHoldDao.getDailyBudget(
                connectionId = connectionId,
                budgetDate = hold.budgetDate,
            )
            dailySpend?.confirmedSpendSats?.decrypted shouldBe 175L
        }

    @Test
    fun releaseHoldMarksHoldReleased() =
        runTest {
            val connectionId = "connection-5"
            database.nwcConnections().upsert(buildConnectionData(secretPubKey = connectionId))

            val result = budgetManager.placeHold(
                connectionId = connectionId,
                amountSats = 120,
                requestId = "request-1",
                timeoutMs = 5_000,
            )

            val holdId = result.shouldBeInstanceOf<NwcPaymentHoldResult.Placed>().holdId
            budgetManager.releaseHold(holdId = holdId)

            val hold = paymentHoldDao.getHoldById(holdId).shouldNotBeNull()
            hold.status shouldBe NwcPaymentHoldStatus.RELEASED

            val dailySpend = paymentHoldDao.getDailyBudget(
                connectionId = connectionId,
                budgetDate = hold.budgetDate,
            )
            dailySpend shouldBe null
        }

    @Test
    fun commitAfterReleaseDoesNotChangeSpend() =
        runTest {
            val connectionId = "connection-6"
            database.nwcConnections().upsert(buildConnectionData(secretPubKey = connectionId))

            val result = budgetManager.placeHold(
                connectionId = connectionId,
                amountSats = 200,
                requestId = "request-1",
                timeoutMs = 5_000,
            )

            val holdId = result.shouldBeInstanceOf<NwcPaymentHoldResult.Placed>().holdId
            budgetManager.releaseHold(holdId = holdId)
            budgetManager.commitHold(holdId = holdId, actualAmountSats = 300)

            val hold = paymentHoldDao.getHoldById(holdId).shouldNotBeNull()
            hold.status shouldBe NwcPaymentHoldStatus.RELEASED

            val dailySpend = paymentHoldDao.getDailyBudget(
                connectionId = connectionId,
                budgetDate = hold.budgetDate,
            )
            dailySpend shouldBe null
        }

    @Test
    fun cleanupExpiredHoldsExpiresOnlyStaleEntries() =
        runTest {
            val connectionId = "connection-7"
            database.nwcConnections().upsert(buildConnectionData(secretPubKey = connectionId))

            val now = Clock.System.now().toEpochMilliseconds()
            val budgetDate = currentBudgetDate()

            val expiredPending = insertHold(
                connectionId = connectionId,
                requestId = "request-expired-pending",
                status = NwcPaymentHoldStatus.PENDING,
                budgetDate = budgetDate,
                expiresAt = now - 1,
            )
            val expiredProcessing = insertHold(
                connectionId = connectionId,
                requestId = "request-expired-processing",
                status = NwcPaymentHoldStatus.PROCESSING,
                budgetDate = budgetDate,
                expiresAt = now - 1,
            )
            val activePending = insertHold(
                connectionId = connectionId,
                requestId = "request-active",
                status = NwcPaymentHoldStatus.PENDING,
                budgetDate = budgetDate,
                expiresAt = now + 60_000,
            )

            budgetManager.cleanupExpiredHolds()

            paymentHoldDao.getHoldById(expiredPending).shouldNotBeNull().status shouldBe NwcPaymentHoldStatus.EXPIRED
            paymentHoldDao.getHoldById(expiredProcessing).shouldNotBeNull().status shouldBe NwcPaymentHoldStatus.EXPIRED
            paymentHoldDao.getHoldById(activePending).shouldNotBeNull().status shouldBe NwcPaymentHoldStatus.PENDING
        }

    private fun buildConnectionData(secretPubKey: String, dailyBudgetSats: Long? = 1_000L) =
        NwcConnectionData(
            secretPubKey = secretPubKey,
            walletId = "wallet-$secretPubKey",
            userId = "user-$secretPubKey",
            servicePubKey = "service-pub-$secretPubKey",
            servicePrivateKey = "service-priv-$secretPubKey".asEncryptable(),
            relay = "wss://relay.example/$secretPubKey".asEncryptable(),
            appName = "Test App".asEncryptable(),
            dailyBudgetSats = dailyBudgetSats?.asEncryptable(),
        )

    private suspend fun insertHold(
        connectionId: String,
        requestId: String,
        status: NwcPaymentHoldStatus,
        budgetDate: String,
        expiresAt: Long,
    ): String {
        val now = Clock.System.now().toEpochMilliseconds()
        val holdId = UUID.randomUUID().toString()
        paymentHoldDao.insertHold(
            NwcPaymentHoldData(
                holdId = holdId,
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
        return holdId
    }

    private fun currentBudgetDate(): String {
        return Clock.System.now().toLocalDateTime(TimeZone.UTC).date.toString()
    }
}
