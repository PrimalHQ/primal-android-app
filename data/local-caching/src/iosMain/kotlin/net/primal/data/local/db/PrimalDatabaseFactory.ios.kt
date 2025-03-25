package net.primal.data.local.db

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.NativeSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import net.primal.core.utils.coroutines.IOSDispatcherProvider
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object PrimalDatabaseFactory {

    private val defaultDatabase: PrimalDatabase by lazy { createDatabase() }

    fun getDefaultDatabase() = defaultDatabase

    fun createDatabase(): PrimalDatabase {
        return buildPrimalDatabase(
            driver = NativeSQLiteDriver(),
            queryCoroutineContext = IOSDispatcherProvider().io(),
            builder = getDatabaseBuilder(),
        )
    }

    private fun getDatabaseBuilder(): RoomDatabase.Builder<PrimalDatabase> {
        val dbFilePath = documentDirectory() + "/$DATABASE_NAME"
        return Room.databaseBuilder<PrimalDatabase>(
            name = dbFilePath,
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun documentDirectory(): String {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null,
        )
        return requireNotNull(documentDirectory?.path)
    }
}
