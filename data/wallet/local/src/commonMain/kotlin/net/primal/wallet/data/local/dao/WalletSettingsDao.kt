package net.primal.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WalletSettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWalletSettings(settings: WalletSettings)

    @Query("SELECT * FROM WalletSettings WHERE walletId = :walletId")
    suspend fun findWalletSettings(walletId: Long): WalletSettings?
}
