package net.primal.wallet.data.local.dao.nwc

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import net.primal.shared.data.local.encryption.Encryptable

@Dao
interface NwcWalletRequestLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: NwcWalletRequestLog)

    @Query("SELECT * FROM NwcWalletRequestLog ORDER BY requestedAt DESC")
    suspend fun getAllLogs(): List<NwcWalletRequestLog>

    @Query(
        """
        UPDATE NwcWalletRequestLog
        SET responsePayload = :responsePayload,
            requestState = :requestState,
            completedAt = :completedAt,
            errorCode = :errorCode,
            errorMessage = :errorMessage
        WHERE eventId = :eventId
    """,
    )
    suspend fun updateResponse(
        eventId: String,
        responsePayload: Encryptable<String>?,
        requestState: Encryptable<String>,
        completedAt: Long,
        errorCode: Encryptable<String>?,
        errorMessage: Encryptable<String>?,
    )

    @Query("DELETE FROM NwcWalletRequestLog WHERE walletId = :walletId")
    suspend fun deleteByWalletId(walletId: String)
}
