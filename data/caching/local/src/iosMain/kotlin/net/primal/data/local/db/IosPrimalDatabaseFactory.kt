package net.primal.data.local.db

import androidx.room.Room
import androidx.sqlite.driver.NativeSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import net.primal.core.utils.coroutines.IOSDispatcherProvider
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

typealias PrimalDatabaseFactory = IosPrimalDatabaseFactory

object IosPrimalDatabaseFactory {

    private val defaultDatabase: PrimalDatabase by lazy { createDatabase() }

    fun getDefaultDatabase() = defaultDatabase

    fun createDatabase(): PrimalDatabase {
        val dbFilePath = documentDirectory() + "/$DATABASE_NAME"
        return buildPrimalDatabase {
            Room.databaseBuilder<PrimalDatabase>(name = dbFilePath)
                .setQueryCoroutineContext(IOSDispatcherProvider().io())
                .setDriver(NativeSQLiteDriver())
        }
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
