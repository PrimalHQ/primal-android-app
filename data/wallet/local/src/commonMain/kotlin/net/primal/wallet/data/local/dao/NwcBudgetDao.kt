package net.primal.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface NwcBudgetDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertReservation(reservation: NwcBudgetReservationData)

    @Update
    suspend fun updateReservation(reservation: NwcBudgetReservationData)

    @Query("SELECT * FROM NwcBudgetReservationData WHERE reservationId = :reservationId")
    suspend fun getReservationById(reservationId: String): NwcBudgetReservationData?

    @Query(
        """
        SELECT * FROM NwcBudgetReservationData
        WHERE connectionId = :connectionId
        AND budgetDate = :budgetDate
        AND status IN ('PENDING', 'PROCESSING')
        """,
    )
    suspend fun getPendingReservations(connectionId: String, budgetDate: String): List<NwcBudgetReservationData>

    @Query(
        """
        UPDATE NwcBudgetReservationData
        SET status = 'EXPIRED', updatedAt = :updatedAt
        WHERE status IN ('PENDING', 'PROCESSING')
        AND expiresAt < :now
        """,
    )
    suspend fun expireStaleReservations(now: Long, updatedAt: Long)

    @Query(
        """
        SELECT * FROM NwcBudgetReservationData
        WHERE status = 'PROCESSING'
        """,
    )
    suspend fun getProcessingReservations(): List<NwcBudgetReservationData>

    @Query("DELETE FROM NwcBudgetReservationData WHERE connectionId = :connectionId")
    suspend fun deleteReservationsByConnectionId(connectionId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailySpend(dailySpend: NwcDailySpendData)

    @Query(
        """
        SELECT * FROM NwcDailySpendData
        WHERE connectionId = :connectionId
        AND budgetDate = :budgetDate
        """,
    )
    suspend fun getDailySpend(connectionId: String, budgetDate: String): NwcDailySpendData?

    @Query("DELETE FROM NwcDailySpendData WHERE connectionId = :connectionId")
    suspend fun deleteDailySpendByConnectionId(connectionId: String)
}
