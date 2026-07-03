package net.primal.wallet.data.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContainIgnoringCase
import java.util.UUID
import kotlinx.coroutines.test.runTest
import net.primal.core.utils.runCatching
import net.primal.shared.data.local.db.buildLocalDatabase
import net.primal.wallet.data.local.db.WalletDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@Suppress("FunctionNaming", "MagicNumber")
@RunWith(RobolectricTestRunner::class)
class LocalDatabaseFactoryDestructiveFallbackTest {

    private lateinit var dbName: String
    private lateinit var context: Context

    @Before
    fun setUp() {
        WalletDatabase.setEncryption(enableEncryption = false)
        context = ApplicationProvider.getApplicationContext()
        dbName = "destructive_fallback_test_${UUID.randomUUID()}.db"
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    /**
     * Creates a database file at user_version 1. Opening it with the current WalletDatabase
     * schema and no registered migrations means Room has a missing migration path.
     */
    private fun createStaleDatabase() {
        val dbFile = context.getDatabasePath(dbName)
        dbFile.parentFile?.mkdirs()
        val connection = AndroidSQLiteDriver().open(dbFile.absolutePath)
        connection.execSQL("PRAGMA user_version = 1")
        connection.execSQL("CREATE TABLE Marker (id INTEGER PRIMARY KEY)")
        connection.execSQL("INSERT INTO Marker (id) VALUES (1)")
        connection.close()
    }

    private fun openDatabase(fallbackToDestructiveMigration: Boolean): WalletDatabase =
        buildLocalDatabase(fallbackToDestructiveMigration = fallbackToDestructiveMigration) {
            Room.databaseBuilder<WalletDatabase>(
                context = context,
                name = context.getDatabasePath(dbName).absolutePath,
            )
                .setDriver(AndroidSQLiteDriver())
                .allowMainThreadQueries()
        }

    @Test
    fun `missing migration throws when destructive fallback is disabled`() =
        runTest {
            createStaleDatabase()
            val database = openDatabase(fallbackToDestructiveMigration = false)

            val result = runCatching {
                database.userWalletPreferences().isNwcAutoStartEnabled("__trigger__")
            }
            database.close()

            result.isFailure shouldBe true
            (result.exceptionOrNull() is IllegalStateException) shouldBe true
            result.exceptionOrNull()?.message.shouldContainIgnoringCase("migration")
        }

    @Test
    fun `missing migration recreates database when destructive fallback is enabled`() =
        runTest {
            createStaleDatabase()
            val database = openDatabase(fallbackToDestructiveMigration = true)

            val result = runCatching {
                database.userWalletPreferences().isNwcAutoStartEnabled("__trigger__")
            }
            database.close()

            result.isSuccess shouldBe true
        }
}
