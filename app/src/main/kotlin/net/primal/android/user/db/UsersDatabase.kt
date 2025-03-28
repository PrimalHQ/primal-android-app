package net.primal.android.user.db

import androidx.room.Database
import androidx.room.RoomDatabase
import net.primal.android.wallet.db.WalletTransactionDao
import net.primal.android.wallet.db.WalletTransactionData

@Database(
    entities = [
        UserProfileInteraction::class,
        WalletTransactionData::class,
        Relay::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class UsersDatabase : RoomDatabase() {

    abstract fun relays(): RelayDao

    abstract fun userProfileInteractions(): UserProfileInteractionDao

    abstract fun walletTransactions(): WalletTransactionDao
}
