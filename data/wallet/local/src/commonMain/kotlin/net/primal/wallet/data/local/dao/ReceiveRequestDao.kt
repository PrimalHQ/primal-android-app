package net.primal.wallet.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReceiveRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(data: ReceiveRequestData)

    @Query(
        """
        SELECT * FROM ReceiveRequestData
        WHERE walletId IS :walletId AND type IS :type AND fulfilledAt IS NULL
            AND createdAt >= :createdAfter
        ORDER BY createdAt DESC
        LIMIT :limit
        """,
    )
    suspend fun findUnfulfilled(
        walletId: String,
        type: ReceiveRequestType,
        createdAfter: Long,
        limit: Int,
    ): List<ReceiveRequestData>

    @Query("UPDATE ReceiveRequestData SET fulfilledAt = :fulfilledAt WHERE id = :id")
    suspend fun markFulfilled(id: Long, fulfilledAt: Long)
}
