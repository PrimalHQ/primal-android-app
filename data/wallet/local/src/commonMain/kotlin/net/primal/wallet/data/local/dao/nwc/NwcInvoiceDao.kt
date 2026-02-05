package net.primal.wallet.data.local.dao.nwc

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface NwcInvoiceDao {

    @Upsert
    suspend fun upsert(data: NwcInvoiceData)

    @Query("SELECT * FROM NwcInvoiceData WHERE invoice = :invoice")
    suspend fun findByInvoice(invoice: String): NwcInvoiceData?

    @Query("SELECT * FROM NwcInvoiceData WHERE paymentHash = :paymentHash")
    suspend fun findByPaymentHash(paymentHash: String): NwcInvoiceData?

    @Query("UPDATE NwcInvoiceData SET state = 'EXPIRED' WHERE invoice = :invoice")
    suspend fun markExpired(invoice: String)

    @Query(
        """
        UPDATE NwcInvoiceData
        SET state = 'SETTLED', settledAt = :settledAt, preimage = :preimage
        WHERE paymentHash = :paymentHash AND state != 'SETTLED'
        """,
    )
    suspend fun markSettledWithDetails(
        paymentHash: String,
        settledAt: Long,
        preimage: String?,
    )
}
