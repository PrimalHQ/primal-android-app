package net.primal.wallet.data.local.dao.nwc

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import net.primal.shared.data.local.encryption.Encryptable

@Dao
interface NwcConnectionDao {
    @Upsert
    suspend fun upsert(data: NwcConnectionData)

    @Transaction
    @Query("SELECT * FROM NwcConnectionData WHERE userId = :userId")
    suspend fun getAllConnectionsByUser(userId: String): List<NwcConnection>

    @Transaction
    @Query("SELECT * FROM NwcConnectionData WHERE userId = :userId")
    fun observeAllConnectionsByUser(userId: String): Flow<List<NwcConnection>>

    @Query("SELECT * FROM NwcConnectionData WHERE secretPubKey = :secretPubKey")
    suspend fun findConnection(secretPubKey: String): NwcConnectionData?

    @Query(
        """
        UPDATE NwcConnectionData
        SET dailyBudgetSats = :dailyBudgetSats
        WHERE secretPubKey = :secretPubKey
        """,
    )
    suspend fun updateDailyBudgetSats(secretPubKey: String, dailyBudgetSats: Encryptable<Long>?)

    @Query("DELETE FROM NwcConnectionData WHERE secretPubKey = :secretPubKey AND userId = :userId")
    suspend fun deleteConnection(userId: String, secretPubKey: String)

    @Query("SELECT secretPubKey FROM NwcConnectionData WHERE userId = :userId")
    suspend fun findConnectionIdsByUserId(userId: String): List<String>

    @Query("SELECT secretPubKey FROM NwcConnectionData WHERE walletId = :walletId")
    suspend fun findConnectionIdsByWalletId(walletId: String): List<String>

    @Query("DELETE FROM NwcConnectionData WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: String)

    @Query("DELETE FROM NwcConnectionData WHERE walletId = :walletId")
    suspend fun deleteAllByWalletId(walletId: String)
}
