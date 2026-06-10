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

@Suppress("FunctionNaming", "UnsafeCallOnNullableType", "MagicNumber", "LargeClass")
@RunWith(RobolectricTestRunner::class)
class WalletDatabaseMigration6to7Test {

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
    private fun createV6Database(): SQLiteConnection {
        val dbFile = context.getDatabasePath(dbName)
        dbFile.parentFile?.mkdirs()
        val driver = AndroidSQLiteDriver()
        val connection = driver.open(dbFile.absolutePath)

        // All CREATE statements taken verbatim from schemas/.../6.json
        connection.execSQL("PRAGMA user_version = 6")

        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS WalletInfo (
                walletId TEXT NOT NULL, type TEXT NOT NULL, balanceInBtc TEXT,
                maxBalanceInBtc TEXT, lastUpdatedAt INTEGER, PRIMARY KEY(walletId))""",
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
                walletId TEXT NOT NULL, seedWords TEXT NOT NULL, backedUp INTEGER NOT NULL,
                primalTxsMigrated INTEGER, primalTxsMigratedUntil INTEGER, PRIMARY KEY(walletId))""",
        )
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
                updatedAt INTEGER NOT NULL, completedAt TEXT, note TEXT, invoice TEXT,
                amountInBtc TEXT NOT NULL, totalFeeInBtc TEXT, otherUserId TEXT,
                zappedEntity TEXT, zappedByUserId TEXT, txKind TEXT NOT NULL,
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
                eventId TEXT NOT NULL, userId TEXT NOT NULL, connectionId TEXT NOT NULL,
                rawNostrEventJson TEXT NOT NULL, createdAt INTEGER NOT NULL,
                PRIMARY KEY(eventId))""",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS ReceiveRequestData (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, walletId TEXT NOT NULL,
                type TEXT NOT NULL, createdAt INTEGER NOT NULL, fulfilledAt INTEGER,
                payload TEXT NOT NULL, amountInBtc TEXT)""",
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
            "CREATE INDEX IF NOT EXISTS index_ZapEnrichmentTracker_invoice ON ZapEnrichmentTracker (invoice)",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS index_ZapEnrichmentTracker_status_lastAttemptAt " +
                "ON ZapEnrichmentTracker (status, lastAttemptAt)",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS WalletUserLink (
                userId TEXT NOT NULL, walletId TEXT NOT NULL, lightningAddress TEXT,
                PRIMARY KEY(userId, walletId))""",
        )
        connection.execSQL(
            "CREATE INDEX IF NOT EXISTS index_WalletUserLink_walletId ON WalletUserLink (walletId)",
        )
        connection.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS " +
                "index_WalletUserLink_lightningAddress ON WalletUserLink (lightningAddress)",
        )
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS UserWalletPreferences (
                userId TEXT NOT NULL, nwcAutoStart INTEGER NOT NULL, PRIMARY KEY(userId))""",
        )

        // Room master table (identity hash matches v6 schema)
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS room_master_table " +
                "(id INTEGER PRIMARY KEY, identity_hash TEXT)",
        )
        connection.execSQL(
            "INSERT OR REPLACE INTO room_master_table (id, identity_hash) " +
                "VALUES(42, '89f980d1a5e8e16cfece1714047ae5cc')",
        )

        return connection
    }

    @Test
    fun `A custodial-only user has all Primal rows removed`() =
        runTest {
            val v6 = createV6Database()
            v6.execSQL(
                """INSERT INTO WalletInfo (walletId, type, balanceInBtc, lastUpdatedAt)
                VALUES ('primal-A', 'PRIMAL', '0.001', 1000)""",
            )
            v6.execSQL("INSERT INTO PrimalWalletData (walletId, kycLevel) VALUES ('primal-A', 'None')")
            v6.execSQL(
                "INSERT INTO ActiveWalletData (userId, walletId) VALUES ('user-A', 'primal-A')",
            )
            v6.execSQL(
                """INSERT INTO WalletTransactionData (
                    transactionId, walletId, walletType, type, state, createdAt, updatedAt,
                    amountInBtc, txKind
                ) VALUES ('tx1', 'primal-A', 'PRIMAL', 'DEPOSIT', 'COMPLETED',
                    1000, 1000, '0.001', 'LIGHTNING')""",
            )
            v6.execSQL(
                """INSERT INTO WalletTransactionData (
                    transactionId, walletId, walletType, type, state, createdAt, updatedAt,
                    amountInBtc, txKind
                ) VALUES ('tx2', 'primal-A', 'PRIMAL', 'WITHDRAW', 'COMPLETED',
                    1001, 1001, '0.0005', 'LIGHTNING')""",
            )
            v6.execSQL(
                "INSERT INTO WalletUserLink (userId, walletId) VALUES ('user-A', 'primal-A')",
            )
            v6.execSQL(
                "INSERT INTO WalletSettings (walletId, spamThresholdAmountInSats) VALUES ('primal-A', '\"500\"')",
            )
            v6.close()

            val database = openDatabaseWithMigration()

            // Trigger migration by issuing a DAO read (Room opens the DB lazily).
            // WalletInfo for primal-A is gone.
            database.wallet().findWalletInfo("primal-A") shouldBe null

            // ActiveWalletData for the user is gone.
            database.wallet().getActiveWallet("user-A") shouldBe null

            // No WalletInfo rows remain for the user.
            database.wallet().findWalletInfosByUserId("user-A") shouldBe emptyList()

            database.close()

            // PrimalWalletData table is gone (after migration completed).
            tableExists("PrimalWalletData") shouldBe false

            // No surviving WalletTransactionData rows for primal-A.
            countRows("WalletTransactionData", "walletId = 'primal-A'") shouldBe 0

            // No surviving WalletUserLink rows for primal-A.
            countRows("WalletUserLink", "walletId = 'primal-A'") shouldBe 0

            // No surviving WalletSettings rows for primal-A.
            countRows("WalletSettings", "walletId = 'primal-A'") shouldBe 0
        }

    @Test
    fun `B migrated user keeps Spark wallet and drops Primal rows`() =
        runTest {
            val v6 = createV6Database()
            // Primal wallet
            v6.execSQL(
                """INSERT INTO WalletInfo (walletId, type, balanceInBtc, lastUpdatedAt)
                VALUES ('p', 'PRIMAL', '0.001', 1000)""",
            )
            v6.execSQL("INSERT INTO PrimalWalletData (walletId, kycLevel) VALUES ('p', 'None')")
            v6.execSQL(
                "INSERT INTO WalletUserLink (userId, walletId) VALUES ('user-B', 'p')",
            )
            // Spark wallet (active)
            v6.execSQL(
                """INSERT INTO WalletInfo (walletId, type, balanceInBtc, lastUpdatedAt)
                VALUES ('s', 'SPARK', '0.002', 2000)""",
            )
            v6.execSQL(
                """INSERT INTO SparkWalletData (
                    walletId, seedWords, backedUp, primalTxsMigrated, primalTxsMigratedUntil
                ) VALUES ('s', '"twelve word seed phrase here"', 1, 1, 999)""",
            )
            v6.execSQL(
                "INSERT INTO ActiveWalletData (userId, walletId) VALUES ('user-B', 's')",
            )
            v6.execSQL(
                "INSERT INTO WalletUserLink (userId, walletId) VALUES ('user-B', 's')",
            )
            // Mixed transactions
            v6.execSQL(
                """INSERT INTO WalletTransactionData (
                    transactionId, walletId, walletType, type, state, createdAt, updatedAt,
                    amountInBtc, txKind
                ) VALUES ('tx-p1', 'p', 'PRIMAL', 'DEPOSIT', 'COMPLETED',
                    1000, 1000, '0.001', 'LIGHTNING')""",
            )
            v6.execSQL(
                """INSERT INTO WalletTransactionData (
                    transactionId, walletId, walletType, type, state, createdAt, updatedAt,
                    amountInBtc, txKind
                ) VALUES ('tx-s1', 's', 'SPARK', 'DEPOSIT', 'COMPLETED',
                    2000, 2000, '0.002', 'LIGHTNING')""",
            )
            // Settings rows for both wallets — only Primal one should be removed.
            v6.execSQL(
                "INSERT INTO WalletSettings (walletId, spamThresholdAmountInSats) VALUES ('p', '\"100\"')",
            )
            v6.execSQL(
                "INSERT INTO WalletSettings (walletId, spamThresholdAmountInSats) VALUES ('s', '\"200\"')",
            )
            v6.close()

            val database = openDatabaseWithMigration()

            // Primal wallet info gone, Spark survives.
            database.wallet().findWalletInfo("p") shouldBe null
            val sparkInfo = database.wallet().findWalletInfo("s")
            sparkInfo shouldBe notNull()
            sparkInfo!!.walletId shouldBe "s"
            sparkInfo.balanceInBtc!!.decrypted shouldBe 0.002

            // Spark wallet data preserved with only the 3 surviving columns.
            val sparkData = database.wallet().findSparkWalletData("s")
            sparkData shouldBe notNull()
            sparkData!!.walletId shouldBe "s"
            sparkData.seedWords.decrypted shouldBe "twelve word seed phrase here"
            sparkData.backedUp shouldBe true

            // ActiveWalletData→Spark intact.
            val active = database.wallet().getActiveWallet("user-B")
            active shouldBe notNull()
            active!!.active.walletId shouldBe "s"

            // Only SPARK transaction survives.
            countRows("WalletTransactionData", "walletType = 'SPARK'") shouldBe 1
            countRows("WalletTransactionData", "walletType = 'PRIMAL'") shouldBe 0

            // WalletUserLink: only the Spark one remains.
            database.wallet().findWalletUserLink("user-B", "p") shouldBe null
            database.wallet().findWalletUserLink("user-B", "s") shouldBe notNull()

            // SparkWalletData has exactly 3 columns.
            getTableColumns("SparkWalletData") shouldBe setOf("walletId", "seedWords", "backedUp")

            // PrimalWalletData table dropped.
            tableExists("PrimalWalletData") shouldBe false

            // WalletSettings: Primal row gone, Spark row survives.
            countRows("WalletSettings", "walletId = 'p'") shouldBe 0
            countRows("WalletSettings", "walletId = 's'") shouldBe 1

            database.close()
        }

    @Test
    fun `C NWC-only user is untouched`() =
        runTest {
            val v6 = createV6Database()
            v6.execSQL(
                """INSERT INTO WalletInfo (walletId, type, balanceInBtc, lastUpdatedAt)
                VALUES ('n', 'NWC', '0.003', 3000)""",
            )
            v6.execSQL(
                """INSERT INTO NostrWalletData (
                    walletId, relays, pubkey, walletPubkey, walletPrivateKey
                ) VALUES ('n', '["wss://relay.example"]', '"pub"', '"wpub"', '"wpriv"')""",
            )
            v6.execSQL(
                "INSERT INTO ActiveWalletData (userId, walletId) VALUES ('user-C', 'n')",
            )
            v6.execSQL(
                "INSERT INTO WalletUserLink (userId, walletId) VALUES ('user-C', 'n')",
            )
            v6.execSQL(
                """INSERT INTO WalletTransactionData (
                    transactionId, walletId, walletType, type, state, createdAt, updatedAt,
                    amountInBtc, txKind
                ) VALUES ('tx-n1', 'n', 'NWC', 'DEPOSIT', 'COMPLETED',
                    3000, 3000, '0.003', 'LIGHTNING')""",
            )
            v6.close()

            val database = openDatabaseWithMigration()

            val nwcInfo = database.wallet().findWalletInfo("n")
            nwcInfo shouldBe notNull()
            nwcInfo!!.walletId shouldBe "n"

            val active = database.wallet().getActiveWallet("user-C")
            active shouldBe notNull()
            active!!.active.walletId shouldBe "n"

            countRows("NostrWalletData", "walletId = 'n'") shouldBe 1
            countRows("WalletTransactionData", "walletId = 'n'") shouldBe 1
            database.wallet().findWalletUserLink("user-C", "n") shouldBe notNull()

            // PrimalWalletData table is gone, but NWC data is unaffected.
            tableExists("PrimalWalletData") shouldBe false

            database.close()
        }

    @Test
    fun `D Spark-only new user has primalTxsMigrated columns dropped`() =
        runTest {
            val v6 = createV6Database()
            v6.execSQL(
                """INSERT INTO WalletInfo (walletId, type, balanceInBtc, lastUpdatedAt)
                VALUES ('s', 'SPARK', '0.004', 4000)""",
            )
            v6.execSQL(
                """INSERT INTO SparkWalletData (
                    walletId, seedWords, backedUp, primalTxsMigrated, primalTxsMigratedUntil
                ) VALUES ('s', '"another seed"', 1, 1, 42)""",
            )
            v6.execSQL(
                "INSERT INTO ActiveWalletData (userId, walletId) VALUES ('user-D', 's')",
            )
            v6.execSQL(
                "INSERT INTO WalletUserLink (userId, walletId) VALUES ('user-D', 's')",
            )
            v6.close()

            val database = openDatabaseWithMigration()

            val sparkData = database.wallet().findSparkWalletData("s")
            sparkData shouldBe notNull()
            sparkData!!.walletId shouldBe "s"
            sparkData.seedWords.decrypted shouldBe "another seed"
            sparkData.backedUp shouldBe true

            val info = database.wallet().findWalletInfo("s")
            info shouldBe notNull()
            info!!.walletId shouldBe "s"

            val active = database.wallet().getActiveWallet("user-D")
            active shouldBe notNull()
            active!!.active.walletId shouldBe "s"

            getTableColumns("SparkWalletData") shouldBe setOf("walletId", "seedWords", "backedUp")
            tableExists("PrimalWalletData") shouldBe false

            database.close()
        }

    @Test
    fun `E empty database migrates without errors`() =
        runTest {
            val v6 = createV6Database()
            v6.close()

            val database = openDatabaseWithMigration()

            database.wallet().findWalletInfosByUserId("nobody") shouldBe emptyList()
            tableExists("PrimalWalletData") shouldBe false
            getTableColumns("SparkWalletData") shouldBe setOf("walletId", "seedWords", "backedUp")

            database.close()
        }

    @Test
    fun `F orphan rows are cleaned up by transitive filters`() =
        runTest {
            val v6 = createV6Database()

            // Orphan #1: WalletInfo PRIMAL with no matching PrimalWalletData
            v6.execSQL(
                """INSERT INTO WalletInfo (walletId, type)
                VALUES ('p1', 'PRIMAL')""",
            )

            // Orphan #2: ActiveWalletData pointing to a missing WalletInfo
            // (no row inserted for 'p2' in WalletInfo)
            v6.execSQL(
                "INSERT INTO ActiveWalletData (userId, walletId) VALUES ('orphan-user', 'p2')",
            )

            // Orphan #3: WalletTransactionData (walletType='PRIMAL') with no matching parent
            v6.execSQL(
                """INSERT INTO WalletTransactionData (
                    transactionId, walletId, walletType, type, state, createdAt, updatedAt,
                    amountInBtc, txKind
                ) VALUES ('tx-orphan', 'p3', 'PRIMAL', 'DEPOSIT', 'COMPLETED',
                    1000, 1000, '0.001', 'LIGHTNING')""",
            )

            v6.close()

            val database = openDatabaseWithMigration()

            // All orphan PRIMAL rows are gone.
            database.wallet().findWalletInfo("p1") shouldBe null
            countRows("WalletInfo", "type = 'PRIMAL'") shouldBe 0
            countRows("WalletTransactionData", "walletType = 'PRIMAL'") shouldBe 0

            // ActiveWalletData orphans (whose WalletInfo was missing pre-migration) are cleaned up.
            database.wallet().getActiveWallet("orphan-user") shouldBe null

            // PrimalWalletData table dropped.
            tableExists("PrimalWalletData") shouldBe false

            // SparkWalletData is unchanged in shape (no rows in this scenario).
            getTableColumns("SparkWalletData") shouldBe setOf("walletId", "seedWords", "backedUp")

            database.close()
        }

    private fun openDatabaseWithMigration(): WalletDatabase {
        return Room.databaseBuilder<WalletDatabase>(
            context = context,
            name = dbName,
        )
            .setDriver(AndroidSQLiteDriver())
            .addMigrations(WalletDatabase.MIGRATION_5_6, WalletDatabase.MIGRATION_6_7)
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

    private fun tableExists(tableName: String): Boolean {
        val driver = AndroidSQLiteDriver()
        val connection = driver.open(context.getDatabasePath(dbName).absolutePath)
        val stmt = connection.prepare(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
        )
        stmt.bindText(1, tableName)
        val exists = stmt.step()
        stmt.close()
        connection.close()
        return exists
    }

    private fun countRows(tableName: String, whereClause: String): Int {
        val driver = AndroidSQLiteDriver()
        val connection = driver.open(context.getDatabasePath(dbName).absolutePath)
        val stmt = connection.prepare("SELECT COUNT(*) FROM $tableName WHERE $whereClause")
        stmt.step()
        val count = stmt.getLong(0).toInt()
        stmt.close()
        connection.close()
        return count
    }

    private fun notNull() = io.kotest.matchers.nulls.beNull().invert()
}
