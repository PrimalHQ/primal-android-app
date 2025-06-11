package net.primal.shared.data.local.db

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File
import net.primal.core.utils.coroutines.JvmDispatcherProvider

typealias LocalDatabaseFactory = JvmLocalDatabaseFactory

object JvmLocalDatabaseFactory {

    inline fun <reified T : RoomDatabase> createDatabase(databaseName: String): T {
        return buildLocalDatabase {
            val dbFile = File(System.getProperty("java.io.tmpdir"), databaseName)
            Room.databaseBuilder<T>(name = dbFile.absolutePath)
                .setDriver(BundledSQLiteDriver())
                .setQueryCoroutineContext(JvmDispatcherProvider().io())
        }
    }
}
