package net.primal.data.local.db

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import net.primal.core.utils.coroutines.JvmDispatcherProvider

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object PrimalDatabaseFactory {

    private val defaultDatabase: PrimalDatabase by lazy { createDatabase() }

    fun createDatabase(): PrimalDatabase {
        return buildPrimalDatabase(
            driver = BundledSQLiteDriver(),
            queryCoroutineContext = JvmDispatcherProvider().io(),
            builder = getDatabaseBuilder(),
        )
    }

    private fun getDatabaseBuilder(): RoomDatabase.Builder<PrimalDatabase> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), DATABASE_NAME)
        return Room.databaseBuilder<PrimalDatabase>(
            name = dbFile.absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(JvmDispatcherProvider().io())
    }
}
