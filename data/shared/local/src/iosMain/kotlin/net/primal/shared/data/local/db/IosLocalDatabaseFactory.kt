package net.primal.shared.data.local.db

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.NativeSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import net.primal.core.utils.coroutines.IOSDispatcherProvider
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

typealias LocalDatabaseFactory = IosLocalDatabaseFactory

object IosLocalDatabaseFactory {

    inline fun <reified T : RoomDatabase> createDatabase(
        databaseName: String,
        callback: RoomDatabase.Callback? = null,
    ): T {
        val dbFilePath = documentDirectory() + "/$databaseName"
        return buildLocalDatabase {
            Room.databaseBuilder<T>(name = dbFilePath)
                .setQueryCoroutineContext(IOSDispatcherProvider().io())
                .setDriver(NativeSQLiteDriver())
                .run {
                    if (callback != null) {
                        this.addCallback(callback)
                    } else {
                        this
                    }
                }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun documentDirectory(): String {
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
