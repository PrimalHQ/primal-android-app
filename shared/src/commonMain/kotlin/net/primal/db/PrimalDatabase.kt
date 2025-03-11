package net.primal.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.SQLiteDriver
import kotlin.coroutines.CoroutineContext
import net.primal.events.db.Event
import net.primal.events.db.EventsDao
import net.primal.serialization.room.JsonTypeConverters
import net.primal.serialization.room.ListsTypeConverters

@Database(
    entities = [
        Event::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(
    ListsTypeConverters::class,
    JsonTypeConverters::class,
//    AttachmentTypeConverters::class,
//    ProfileTypeConverters::class,
)
abstract class PrimalDatabase : RoomDatabase() {
    abstract fun events(): EventsDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal expect object AppDatabaseConstructor : RoomDatabaseConstructor<PrimalDatabase> {
    override fun initialize(): PrimalDatabase
}

internal fun buildPrimalDatabase(
    driver: SQLiteDriver,
    queryCoroutineContext: CoroutineContext,
    builder: RoomDatabase.Builder<PrimalDatabase>,
): PrimalDatabase {
    return builder
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .setDriver(driver)
        .setQueryCoroutineContext(queryCoroutineContext)
        .build()
}
