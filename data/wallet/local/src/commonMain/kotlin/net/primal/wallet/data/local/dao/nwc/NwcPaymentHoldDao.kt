package net.primal.wallet.data.local.dao.nwc

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface NwcPaymentHoldDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertHold(hold: NwcPaymentHoldData)

    @Update
    suspend fun updateHold(hold: NwcPaymentHoldData)

    @Query("SELECT * FROM NwcPaymentHoldData WHERE holdId = :holdId")
    suspend fun getHoldById(holdId: String): NwcPaymentHoldData?

    @Query(
        """
        SELECT * FROM NwcPaymentHoldData
        WHERE connectionId = :connectionId
        AND budgetDate = :budgetDate
        AND status IN ('PENDING', 'PROCESSING')
        """,
    )
    suspend fun getPendingHolds(connectionId: String, budgetDate: String): List<NwcPaymentHoldData>

    @Query(
        """
        UPDATE NwcPaymentHoldData
        SET status = 'EXPIRED', updatedAt = :updatedAt
        WHERE status IN ('PENDING', 'PROCESSING')
        AND expiresAt < :now
        """,
    )
    suspend fun expireStaleHolds(now: Long, updatedAt: Long)

    @Query(
        """
        SELECT * FROM NwcPaymentHoldData
        WHERE status = 'PROCESSING'
        """,
    )
    suspend fun getProcessingHolds(): List<NwcPaymentHoldData>

    @Query("DELETE FROM NwcPaymentHoldData WHERE connectionId = :connectionId")
    suspend fun deleteHoldsByConnectionId(connectionId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyBudget(dailySpend: NwcDailyBudgetData)

    @Query(
        """
        SELECT * FROM NwcDailyBudgetData
        WHERE connectionId = :connectionId
        AND budgetDate = :budgetDate
        """,
    )
    suspend fun getDailyBudget(connectionId: String, budgetDate: String): NwcDailyBudgetData?

    @Query("DELETE FROM NwcDailyBudgetData WHERE connectionId = :connectionId")
    suspend fun deleteDailyBudgetByConnectionId(connectionId: String)
}
