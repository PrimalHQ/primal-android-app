package net.primal.shared.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import net.primal.core.utils.coroutines.AndroidDispatcherProvider

typealias LocalDatabaseFactory = AndroidLocalDatabaseFactory

object AndroidLocalDatabaseFactory {

    inline fun <reified T : RoomDatabase> createDatabase(
        context: Context,
        databaseName: String,
        fallbackToDestructiveMigration: Boolean,
        callback: RoomDatabase.Callback? = null,
    ): T {
        val appContext = context.applicationContext
        val dbFile = context.getDatabasePath(databaseName)
        return buildLocalDatabase {
            Room.databaseBuilder<T>(context = appContext, name = dbFile.absolutePath)
                .fallbackToDestructiveMigration(dropAllTables = fallbackToDestructiveMigration)
                .setQueryCoroutineContext(AndroidDispatcherProvider().io())
                .setDriver(AndroidSQLiteDriver())
                .run {
                    if (callback != null) {
                        this.addCallback(callback)
                    } else {
                        this
                    }
                }
        }
    }
}
