package net.primal.android.user.db

import androidx.room.Database
import androidx.room.RoomDatabase
import net.primal.android.profile.db.ProfileInteraction
import net.primal.android.profile.db.ProfileInteractionDao
import net.primal.android.wallet.db.WalletTransactionDao
import net.primal.android.wallet.db.WalletTransactionData

@Database(
    entities = [
        ProfileInteraction::class,
        WalletTransactionData::class,
        Relay::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class UsersDatabase : RoomDatabase() {

    abstract fun relays(): RelayDao

    abstract fun profileInteractions(): ProfileInteractionDao

    abstract fun walletTransactions(): WalletTransactionDao
}
