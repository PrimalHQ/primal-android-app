package net.primal.data.local.dao.events

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface EventZapDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(data: EventZap)

    @Query(
        """
            DELETE FROM EventZap
            WHERE zapSenderId = :senderId AND zapReceiverId = :receiverId AND eventId = :noteId
                AND (zapRequestAt = :timestamp OR zapReceiptAt = :timestamp)
        """,
    )
    suspend fun delete(
        senderId: String,
        receiverId: String,
        noteId: String,
        timestamp: Long,
    )

    @Query("DELETE FROM EventZap WHERE eventId = :eventId")
    suspend fun deleteAll(eventId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<EventZap>)

    @Transaction
    @Query(
        """
            SELECT * FROM EventZap WHERE eventId = :eventId
            ORDER BY CAST(amountInBtc AS REAL) DESC, zapReceiptAt ASC
        """,
    )
    fun pagedEventZaps(eventId: String): PagingSource<Int, EventZap>
}
