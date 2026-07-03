package net.primal.wallet.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.useWriterConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import java.io.File
import java.util.UUID
import kotlinx.coroutines.test.runTest
import net.primal.shared.data.local.db.buildLocalDatabase
import net.primal.wallet.data.local.dao.UserWalletPreferences
import net.primal.wallet.data.local.db.WalletDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("FunctionNaming", "MagicNumber")
@RunWith(RobolectricTestRunner::class)
class DatabaseSpaceReclaimTest {

    private lateinit var dbName: String
    private lateinit var context: Context

    @Before
    fun setUp() {
        WalletDatabase.setEncryption(enableEncryption = false)
        context = ApplicationProvider.getApplicationContext()
        dbName = "space_reclaim_test_${UUID.randomUUID()}.db"
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    private fun dbFile(): File = context.getDatabasePath(dbName)

    private fun walFile(): File = File(dbFile().absolutePath + "-wal")

    /**
     * Creates a database file at user_version 1 with ~6 MB of junk data. Opening it with
     * the current WalletDatabase schema and no registered migrations forces Room into
     * the destructive migration path.
     */
    private fun createStaleDatabaseWithJunk() {
        val dbFile = dbFile()
        dbFile.parentFile?.mkdirs()
        val connection = AndroidSQLiteDriver().open(dbFile.absolutePath)
        // Android's driver defaults new files to auto_vacuum=FULL, which would shrink the main
        // file on the destructive drop even without VACUUM. Disable it to match the plain-SQLite
        // default so the test depends on the callback's VACUUM actually running. The driver has
        // already created android_metadata by this point, so the pragma alone is a no-op — it
        // needs an immediate VACUUM to rewrite the file. It is a persistent database-file
        // property, so Room's later connections keep it.
        connection.execSQL("PRAGMA auto_vacuum = 0")
        connection.execSQL("VACUUM")
        connection.execSQL("PRAGMA user_version = 1")
        connection.execSQL(
            "CREATE TABLE JunkData (id INTEGER PRIMARY KEY AUTOINCREMENT, payload TEXT NOT NULL)",
        )
        val payload = "x".repeat(10_000)
        repeat(600) {
            connection.execSQL("INSERT INTO JunkData (payload) VALUES ('$payload')")
        }
        connection.close()
    }

    private fun openDatabase(): WalletDatabase =
        buildLocalDatabase(fallbackToDestructiveMigration = true) {
            Room.databaseBuilder<WalletDatabase>(context = context, name = dbFile().absolutePath)
                .setDriver(AndroidSQLiteDriver())
                .allowMainThreadQueries()
        }

    @Test
    fun `destructive migration reclaims main database file space`() =
        runTest {
            createStaleDatabaseWithJunk()
            val sizeBefore = dbFile().length()
            sizeBefore shouldBeGreaterThan 4L * 1024 * 1024

            val database = openDatabase()
            // Room opens connections lazily; a query forces the open + destructive migration.
            database.userWalletPreferences().isNwcAutoStartEnabled("__trigger__")

            val sizeAfter = dbFile().length()
            database.close()

            sizeAfter shouldBeLessThan 1L * 1024 * 1024
        }

    @Test
    fun `wal file is truncated after destructive migration`() =
        runTest {
            createStaleDatabaseWithJunk()

            val database = openDatabase()
            database.userWalletPreferences().isNwcAutoStartEnabled("__trigger__")

            val walSize = walFile().length()
            database.close()

            walSize shouldBeLessThan 4L * 1024 * 1024
        }

    @Test
    fun `journal size limit is applied to opened connections`() =
        runTest {
            val database = openDatabase()

            val limit = database.useWriterConnection { transactor ->
                transactor.usePrepared("PRAGMA journal_size_limit") { statement ->
                    statement.step()
                    statement.getLong(0)
                }
            }
            database.close()

            limit shouldBe 4L * 1024 * 1024
        }

    @Test
    fun `regular reopen preserves existing data`() =
        runTest {
            val first = openDatabase()
            first.userWalletPreferences().upsertPreferences(
                UserWalletPreferences(userId = "user1", nwcAutoStart = false),
            )
            first.close()

            val second = openDatabase()
            val nwcAutoStart = second.userWalletPreferences().isNwcAutoStartEnabled("user1")
            second.close()

            nwcAutoStart shouldBe false
        }
}
