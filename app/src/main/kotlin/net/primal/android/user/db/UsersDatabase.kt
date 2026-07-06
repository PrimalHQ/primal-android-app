package net.primal.android.user.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileInteraction::class,
        Relay::class,
        RecentSearch::class,
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
    ],
)
abstract class UsersDatabase : RoomDatabase() {

    abstract fun relays(): RelayDao

    abstract fun userProfileInteractions(): UserProfileInteractionDao

    abstract fun recentSearches(): RecentSearchDao
}
