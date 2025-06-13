package net.primal.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.primal.domain.wallet.WalletType

@Dao
interface WalletDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWalletInfo(info: WalletInfo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWalletBalance(balance: WalletBalance)

    @Query("SELECT * FROM WalletInfo WHERE userId = :userId")
    suspend fun findWalletInfo(userId: String): List<WalletInfo>

    @Query("SELECT * FROM WalletInfo WHERE userId = :userId AND type = :type")
    suspend fun findWalletInfo(userId: String, type: WalletType): WalletInfo?
}
