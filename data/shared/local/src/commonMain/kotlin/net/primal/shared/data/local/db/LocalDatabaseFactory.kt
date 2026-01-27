package net.primal.shared.data.local.db

import androidx.room.RoomDatabase
import androidx.room.migration.Migration

fun <T : RoomDatabase> buildLocalDatabase(
    fallbackToDestructiveMigration: Boolean,
    migrations: List<Migration> = emptyList(),
    createDatabaseBuilder: () -> RoomDatabase.Builder<T>,
): T {
    return createDatabaseBuilder()
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .fallbackToDestructiveMigration(fallbackToDestructiveMigration)
        .run {
            if (migrations.isNotEmpty()) {
                addMigrations(*migrations.toTypedArray())
            } else {
                this
            }
        }
        .build()
}
