package net.primal.shared.data.local.db

import androidx.room.RoomDatabase

fun <T : RoomDatabase> buildLocalDatabase(
    fallbackToDestructiveMigration: Boolean,
    createDatabaseBuilder: () -> RoomDatabase.Builder<T>,
): T {
    return createDatabaseBuilder()
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .fallbackToDestructiveMigration(fallbackToDestructiveMigration)
        .build()
}
