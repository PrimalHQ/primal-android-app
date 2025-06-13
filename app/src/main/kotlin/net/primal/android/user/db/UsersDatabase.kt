package net.primal.android.user.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileInteraction::class,
        Relay::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class UsersDatabase : RoomDatabase() {

    abstract fun relays(): RelayDao

    abstract fun userProfileInteractions(): UserProfileInteractionDao
}
