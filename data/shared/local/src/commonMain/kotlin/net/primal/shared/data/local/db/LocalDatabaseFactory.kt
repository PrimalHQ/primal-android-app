package net.primal.shared.data.local.db

import androidx.room.RoomDatabase

fun <T : RoomDatabase> buildLocalDatabase(createDatabaseBuilder: () -> RoomDatabase.Builder<T>): T {
    return createDatabaseBuilder()
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .fallbackToDestructiveMigration(true)
        .build()
}
