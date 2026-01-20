package net.primal.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import net.primal.shared.data.local.encryption.Encryptable

@Dao
interface NostrWalletConnectionDao {
    @Upsert
    suspend fun upsert(data: NostrWalletConnectionData)

    @Transaction
    @Query("SELECT * FROM NostrWalletConnectionData WHERE userId = :userId")
    suspend fun getAllConnectionsByUser(userId: String): List<NostrWalletConnection>

    @Transaction
    @Query("SELECT * FROM NostrWalletConnectionData WHERE userId = :userId")
    fun observeAllConnectionsByUser(userId: String): Flow<List<NostrWalletConnection>>

    @Query("SELECT * FROM NostrWalletConnectionData WHERE secretPubKey = :secretPubKey")
    suspend fun findConnection(secretPubKey: String): NostrWalletConnectionData?

    @Query(
        """
        UPDATE NostrWalletConnectionData
        SET dailyBudgetSats = :dailyBudgetSats
        WHERE secretPubKey = :secretPubKey
        """,
    )
    suspend fun updateDailyBudgetSats(secretPubKey: String, dailyBudgetSats: Encryptable<Long>?)
}
