package net.primal.wallet.data.local.dao.nwc

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("DELETE FROM NwcWalletRequestLog WHERE userId = :userId")
    suspend fun deleteAllByUserId(userId: String)
}
