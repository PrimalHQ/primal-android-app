package net.primal.wallet.data.migration

import android.content.Context
import androidx.room.Room
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import java.util.UUID
import kotlinx.coroutines.test.runTest
import net.primal.wallet.data.local.db.WalletDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("FunctionNaming", "UnsafeCallOnNullableType", "MagicNumber")
@RunWith(RobolectricTestRunner::class)
class WalletDatabaseMigration5to6Test {

    private lateinit var dbName: String
    private lateinit var context: Context

    @Before
    fun setUp() {
        WalletDatabase.setEncryption(enableEncryption = false)
        context = ApplicationProvider.getApplicationContext()
        dbName = "wallet_migration_test_${UUID.randomUUID()}.db"
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    @Suppress("LongMethod")
    private fun createV5Database(): SQLiteConnection {
        val dbFile = context.getDatabasePath(dbName)
        dbFile.parentFile?.mkdirs()
        val driver = AndroidSQLiteDriver()
        val connection = driver.open(dbFile.absolutePath)

        // All CREATE statements taken verbatim from schemas/.../5.json
        connection.execSQL("PRAGMA user_version = 5")

        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS WalletInfo (
                walletId TEXT NOT NULL, userId TEXT NOT NULL, lightningAddress TEXT,
                type TEXT NOT NULL, balanceInBtc TEXT, maxBalanceInBtc TEXT,
                lastUpdatedAt INTEGER, PRIMARY KEY(walletId))""",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS NostrWalletData (
                walletId TEXT NOT NULL, relays TEXT NOT NULL, pubkey TEXT NOT NULL,
                walletPubkey TEXT NOT NULL, walletPrivateKey TEXT NOT NULL, PRIMARY KEY(walletId))""",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS PrimalWalletData (
                walletId TEXT NOT NULL, kycLevel TEXT NOT NULL, PRIMARY KEY(walletId))""",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS SparkWalletData (
                walletId TEXT NOT NULL, userId TEXT NOT NULL, seedWords TEXT NOT NULL,
                backedUp INTEGER NOT NULL, primalTxsMigrated INTEGER,
                primalTxsMigratedUntil INTEGER, nwcAutoStart INTEGER NOT NULL,
                PRIMARY KEY(walletId))""",
        )
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_SparkWalletData_userId ON SparkWalletData (userId)")
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS ActiveWalletData (
                userId TEXT NOT NULL, walletId TEXT NOT NULL, PRIMARY KEY(userId))""",
        )
        connection.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS " +
                "index_ActiveWalletData_userId_walletId ON ActiveWalletData (userId, walletId)",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS WalletTransactionData (
                transactionId TEXT NOT NULL, walletId TEXT NOT NULL, walletType TEXT NOT NULL,
                type TEXT NOT NULL, state TEXT NOT NULL, createdAt INTEGER NOT NULL,
                updatedAt INTEGER NOT NULL, completedAt TEXT, userId TEXT NOT NULL,
                note TEXT, invoice TEXT, amountInBtc TEXT NOT NULL, totalFeeInBtc TEXT,
                otherUserId TEXT, zappedEntity TEXT, zappedByUserId TEXT, txKind TEXT NOT NULL,
                onChainAddress TEXT, onChainTxId TEXT, preimage TEXT, paymentHash TEXT,
                amountInUsd TEXT, exchangeRate TEXT, otherLightningAddress TEXT,
                PRIMARY KEY(transactionId))""",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS index_WalletTransactionData_invoice ON WalletTransactionData (invoice)",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS index_WalletTransactionData_paymentHash ON WalletTransactionData (paymentHash)",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS WalletTransactionRemoteKey (
                walletId TEXT NOT NULL, transactionId TEXT NOT NULL,
                sinceId INTEGER NOT NULL, untilId INTEGER NOT NULL, cachedAt INTEGER NOT NULL,
                PRIMARY KEY(walletId, transactionId))""",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS WalletSettings (
                walletId TEXT NOT NULL, spamThresholdAmountInSats TEXT NOT NULL, PRIMARY KEY(walletId))""",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS NwcConnectionData (
                secretPubKey TEXT NOT NULL, walletId TEXT NOT NULL, userId TEXT NOT NULL,
                servicePubKey TEXT NOT NULL, servicePrivateKey TEXT NOT NULL,
                relay TEXT NOT NULL, appName TEXT NOT NULL, dailyBudgetSats TEXT,
                PRIMARY KEY(secretPubKey))""",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS NwcPaymentHoldData (
                holdId TEXT NOT NULL, connectionId TEXT NOT NULL, requestId TEXT NOT NULL,
                amountSats TEXT NOT NULL, status TEXT NOT NULL, budgetDate TEXT NOT NULL,
                createdAt INTEGER NOT NULL, expiresAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL,
                PRIMARY KEY(holdId))""",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS index_NwcPaymentHoldData_connectionId ON NwcPaymentHoldData (connectionId)",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS index_NwcPaymentHoldData_budgetDate ON NwcPaymentHoldData (budgetDate)",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS NwcDailyBudgetData (
                connectionId TEXT NOT NULL, budgetDate TEXT NOT NULL,
                confirmedSpendSats TEXT NOT NULL, lastUpdatedAt INTEGER NOT NULL,
                PRIMARY KEY(connectionId, budgetDate))""",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS NwcWalletRequestLog (
                eventId TEXT NOT NULL, connectionId TEXT NOT NULL, walletId TEXT NOT NULL,
                userId TEXT NOT NULL, method TEXT NOT NULL, requestPayload TEXT NOT NULL,
                responsePayload TEXT, requestState TEXT NOT NULL, errorCode TEXT,
                errorMessage TEXT, requestedAt INTEGER NOT NULL, completedAt INTEGER,
                amountMsats TEXT, PRIMARY KEY(eventId))""",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS NwcInvoiceData (
                invoice TEXT NOT NULL, paymentHash TEXT, walletId TEXT NOT NULL,
                connectionId TEXT NOT NULL, description TEXT, descriptionHash TEXT,
                amountMsats TEXT NOT NULL, createdAt INTEGER NOT NULL,
                expiresAt INTEGER NOT NULL, settledAt INTEGER, preimage TEXT,
                PRIMARY KEY(invoice))""",
        )
        connection.execSQL("CREATE INDEX IF NOT EXISTS index_NwcInvoiceData_walletId ON NwcInvoiceData (walletId)")
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS index_NwcInvoiceData_connectionId ON NwcInvoiceData (connectionId)",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS index_NwcInvoiceData_paymentHash ON NwcInvoiceData (paymentHash)",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS NwcPendingEventData (
                eventId TEXT NOT NULL, userId TEXT NOT NULL,
                connectionId TEXT NOT NULL,
                rawNostrEventJson TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                PRIMARY KEY(eventId))""",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS ReceiveRequestData (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, walletId TEXT NOT NULL,
                userId TEXT NOT NULL, type TEXT NOT NULL, createdAt INTEGER NOT NULL,
                fulfilledAt INTEGER, payload TEXT NOT NULL, amountInBtc TEXT)""",
        )
        connection.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS " +
                "index_ReceiveRequestData_walletId_type_payload " +
                "ON ReceiveRequestData (walletId, type, payload)",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS ZapEnrichmentTracker (
                transactionId TEXT NOT NULL, invoice TEXT NOT NULL,
                transactionCreatedAt INTEGER NOT NULL, status TEXT NOT NULL,
                attempts INTEGER NOT NULL, lastAttemptAt INTEGER NOT NULL,
                attemptHistory TEXT NOT NULL DEFAULT '',
                PRIMARY KEY(transactionId))""",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS " +
                "index_ZapEnrichmentTracker_invoice ON ZapEnrichmentTracker (invoice)",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS " +
                "index_ZapEnrichmentTracker_status_lastAttemptAt " +
                "ON ZapEnrichmentTracker (status, lastAttemptAt)",
        )

        // Room master table
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS room_master_table " +
                "(id INTEGER PRIMARY KEY, identity_hash TEXT)",
        )
        connection.execSQL(
            "INSERT OR REPLACE INTO room_master_table (id, identity_hash) " +
                "VALUES(42, 'b0af0847421d773740cc8e2475c7061e')",
        )

        return connection
    }

    @Test
    fun `migration creates WalletUserLink table`() =
        runTest {
            val v5 = createV5Database()
            v5.close()

            val database = openDatabaseWithMigration()
            val link = database.wallet().findWalletUserLink("user1", "wallet1")
            link shouldBe null
            database.close()
        }

    @Test
    fun `migration populates WalletUserLink from WalletInfo`() =
        runTest {
            val v5 = createV5Database()
            v5.execSQL(
                """INSERT INTO WalletInfo (walletId, userId, lightningAddress, type)
                VALUES ('wallet1', 'user1', '"user1@primal.net"', 'SPARK')""",
            )
            v5.execSQL(
                """INSERT INTO WalletInfo (walletId, userId, lightningAddress, type)
                VALUES ('wallet2', 'user2', NULL, 'NWC')""",
            )
            v5.close()

            val database = openDatabaseWithMigration()

            val link1 = database.wallet().findWalletUserLink("user1", "wallet1")
            link1 shouldBe notNull()
            link1!!.lightningAddress!!.decrypted shouldBe "user1@primal.net"

            val link2 = database.wallet().findWalletUserLink("user2", "wallet2")
            link2 shouldBe notNull()
            link2!!.lightningAddress shouldBe null

            database.close()
        }

    @Test
    fun `migration preserves WalletInfo data and removes userId column`() =
        runTest {
            val v5 = createV5Database()
            v5.execSQL(
                """INSERT INTO WalletInfo (walletId, userId, lightningAddress, type, balanceInBtc, lastUpdatedAt)
                VALUES ('wallet1', 'user1', '"user1@primal.net"', 'SPARK', '0.001', 1000)""",
            )
            v5.close()

            val database = openDatabaseWithMigration()
            val walletInfo = database.wallet().findWalletInfo("wallet1")
            walletInfo shouldBe notNull()
            walletInfo!!.walletId shouldBe "wallet1"
            walletInfo.balanceInBtc!!.decrypted shouldBe 0.001
            database.close()

            val columns = getTableColumns("WalletInfo")
            ("userId" in columns) shouldBe false
            ("lightningAddress" in columns) shouldBe false
        }

    @Test
    fun `migration preserves SparkWalletData and removes userId and nwcAutoStart columns`() =
        runTest {
            val v5 = createV5Database()
            v5.execSQL(
                """INSERT INTO SparkWalletData (walletId, userId, seedWords, backedUp, nwcAutoStart)
                VALUES ('wallet1', 'user1', '"seed words here"', 0, 1)""",
            )
            v5.close()

            val database = openDatabaseWithMigration()
            val sparkData = database.wallet().findSparkWalletData("wallet1")
            sparkData shouldBe notNull()
            sparkData!!.walletId shouldBe "wallet1"
            sparkData.seedWords.decrypted shouldBe "seed words here"
            sparkData.backedUp shouldBe false
            database.close()

            val columns = getTableColumns("SparkWalletData")
            ("userId" in columns) shouldBe false
            ("nwcAutoStart" in columns) shouldBe false
        }

    @Test
    fun `migration populates UserWalletPreferences from SparkWalletData`() =
        runTest {
            val v5 = createV5Database()
            v5.execSQL(
                """INSERT INTO SparkWalletData (walletId, userId, seedWords, backedUp, nwcAutoStart)
                VALUES ('wallet1', 'user1', '"seed words"', 0, 0)""",
            )
            v5.close()

            val database = openDatabaseWithMigration()
            val nwcAutoStart = database.userWalletPreferences().isNwcAutoStartEnabled("user1")
            nwcAutoStart shouldBe notNull()
            nwcAutoStart shouldBe false
            database.close()
        }

    @Test
    fun `migration removes userId from WalletTransactionData`() =
        runTest {
            val v5 = createV5Database()
            v5.execSQL(
                """INSERT INTO WalletTransactionData (
                transactionId, userId, walletId, walletType, type, state,
                createdAt, updatedAt, amountInBtc, txKind
            ) VALUES ('tx1', 'user1', 'wallet1', 'SPARK', 'DEPOSIT', 'COMPLETED',
                1000, 1000, '0.001', 'LIGHTNING')""",
            )
            v5.close()

            val database = openDatabaseWithMigration()
            database.close()

            val columns = getTableColumns("WalletTransactionData")
            ("userId" in columns) shouldBe false
            ("transactionId" in columns) shouldBe true
            ("walletId" in columns) shouldBe true
        }

    @Test
    fun `migration removes userId from ReceiveRequestData`() =
        runTest {
            val v5 = createV5Database()
            v5.execSQL(
                """INSERT INTO ReceiveRequestData (userId, walletId, type, createdAt, payload)
            VALUES ('user1', 'wallet1', 'LIGHTNING_INVOICE', 1000, 'lnbc1...')""",
            )
            v5.close()

            val database = openDatabaseWithMigration()
            database.close()

            val columns = getTableColumns("ReceiveRequestData")
            ("userId" in columns) shouldBe false
            ("walletId" in columns) shouldBe true
        }

    @Test
    fun `migration creates separate WalletUserLink rows for multiple wallets per user`() =
        runTest {
            val v5 = createV5Database()
            v5.execSQL(
                """INSERT INTO WalletInfo (walletId, userId, lightningAddress, type)
                VALUES ('spark_wallet', 'user1', '"user1@primal.net"', 'SPARK')""",
            )
            v5.execSQL(
                """INSERT INTO WalletInfo (walletId, userId, lightningAddress, type)
                VALUES ('nwc_wallet', 'user1', NULL, 'NWC')""",
            )
            v5.execSQL(
                """INSERT INTO WalletInfo (walletId, userId, lightningAddress, type)
                VALUES ('other_spark', 'user2', '"user2@primal.net"', 'SPARK')""",
            )
            v5.close()

            val database = openDatabaseWithMigration()

            val user1Spark = database.wallet().findWalletUserLink("user1", "spark_wallet")
            user1Spark shouldBe notNull()
            user1Spark!!.lightningAddress!!.decrypted shouldBe "user1@primal.net"

            val user1Nwc = database.wallet().findWalletUserLink("user1", "nwc_wallet")
            user1Nwc shouldBe notNull()
            user1Nwc!!.lightningAddress shouldBe null

            val user2Spark = database.wallet().findWalletUserLink("user2", "other_spark")
            user2Spark shouldBe notNull()
            user2Spark!!.lightningAddress!!.decrypted shouldBe "user2@primal.net"

            // Cross-user links should not exist
            val crossLink = database.wallet().findWalletUserLink("user1", "other_spark")
            crossLink shouldBe null

            database.close()
        }

    @Test
    fun `migration handles empty database without errors`() =
        runTest {
            val v5 = createV5Database()
            v5.close()

            val database = openDatabaseWithMigration()
            val wallets = database.wallet().findWalletInfosByUserId("nonexistent")
            wallets shouldBe emptyList()
            database.close()
        }

    private fun openDatabaseWithMigration(): WalletDatabase {
        return Room.databaseBuilder<WalletDatabase>(
            context = context,
            name = dbName,
        )
            .setDriver(AndroidSQLiteDriver())
            .addMigrations(WalletDatabase.MIGRATION_5_6)
            .allowMainThreadQueries()
            .build()
    }

    private fun getTableColumns(tableName: String): Set<String> {
        val columns = mutableSetOf<String>()
        val driver = AndroidSQLiteDriver()
        val connection = driver.open(context.getDatabasePath(dbName).absolutePath)
        val stmt = connection.prepare("PRAGMA table_info($tableName)")
        while (stmt.step()) {
            columns.add(stmt.getText(1)) // column 1 is 'name'
        }
        stmt.close()
        connection.close()
        return columns
    }

    private fun notNull() = io.kotest.matchers.nulls.beNull().invert()
}
