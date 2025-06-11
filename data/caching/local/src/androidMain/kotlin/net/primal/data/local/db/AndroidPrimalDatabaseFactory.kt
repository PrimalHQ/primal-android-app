package net.primal.data.local.db

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import net.primal.core.utils.coroutines.AndroidDispatcherProvider

typealias PrimalDatabaseFactory = AndroidPrimalDatabaseFactory

object AndroidPrimalDatabaseFactory {

    private var defaultDatabase: PrimalDatabase? = null

    fun getDefaultDatabase(context: Context): PrimalDatabase {
        return defaultDatabase ?: createDatabase(context = context).also { defaultDatabase = it }
    }

    fun createDatabase(context: Context): PrimalDatabase {
        val appContext = context.applicationContext
        val dbFile = context.getDatabasePath(DATABASE_NAME)

        return buildPrimalDatabase {
            Room.databaseBuilder<PrimalDatabase>(context = appContext, name = dbFile.absolutePath)
                .setQueryCoroutineContext(AndroidDispatcherProvider().io())
                .setDriver(AndroidSQLiteDriver())
        }
    }
}
