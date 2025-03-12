package net.primal.db.events

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface EventZapDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(data: EventZap)

//    @Query(
//        """
//            DELETE FROM EventZap
//            WHERE zapSenderId = :senderId AND zapReceiverId = :receiverId AND eventId = :noteId
//                AND (zapRequestAt = :timestamp OR zapReceiptAt = :timestamp)
//        """,
//    )
//    suspend fun delete(
//        senderId: String,
//        receiverId: String,
//        noteId: String,
//        timestamp: Long,
//    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(data: List<EventZap>)

//    @Transaction
//    @Query(
//        """
//            SELECT * FROM EventZap WHERE eventId = :eventId
//            ORDER BY CAST(amountInBtc AS REAL) DESC, zapReceiptAt ASC
//        """,
//    )
//    fun pagedEventZaps(eventId: String): PagingSource<Int, EventZap>
}
