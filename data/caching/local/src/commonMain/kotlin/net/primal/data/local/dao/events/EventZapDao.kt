package net.primal.data.local.dao.events

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.data.local.db.chunkedQuery
import net.primal.domain.events.ZapKind

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

    @Query("SELECT * FROM EventZap WHERE invoice IN (:invoices)")
    @Suppress("ktlint:standard:function-naming", "FunctionNaming")
    suspend fun _findAllByInvoices(invoices: List<String>): List<EventZap>

    suspend fun findAllByInvoices(invoices: List<String>): List<EventZap> =
        invoices.chunkedQuery { _findAllByInvoices(it) }

    @Query("DELETE FROM EventZap WHERE invoice = :invoice")
    suspend fun deleteByInvoice(invoice: String)

    @Transaction
    @Query("SELECT * FROM EventZap WHERE eventId = :eventId AND zapKind = :zapKind")
    fun observeAllByEventId(eventId: String, zapKind: ZapKind = ZapKind.GENERIC): Flow<List<EventZap>>

    @Transaction
    @Query(
        """
            SELECT * FROM EventZap WHERE eventId = :eventId AND zapKind = :zapKind
            ORDER BY CAST(amountInBtc AS REAL) DESC, zapReceiptAt ASC
        """,
    )
    fun pagedEventZaps(eventId: String, zapKind: ZapKind = ZapKind.GENERIC): PagingSource<Int, EventZap>
}
