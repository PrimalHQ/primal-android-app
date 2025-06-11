package net.primal.data.local.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import net.primal.core.utils.coroutines.JvmDispatcherProvider

typealias PrimalDatabaseFactory = DesktopPrimalDatabaseFactory

object DesktopPrimalDatabaseFactory {

    private val defaultDatabase: PrimalDatabase by lazy { createDatabase() }

    fun createDatabase(): PrimalDatabase {
        return buildPrimalDatabase {
            val dbFile = File(System.getProperty("java.io.tmpdir"), DATABASE_NAME)
            Room.databaseBuilder<PrimalDatabase>(name = dbFile.absolutePath)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(JvmDispatcherProvider().io())
        }
    }
}
