package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingNostrEventDao {
    @Upsert
    suspend fun upsertAll(data: List<PendingNostrEvent>)

    @Query("DELETE FROM PendingNostrEvent WHERE eventId IN (:eventIds)")
    suspend fun deleteByIds(eventIds: List<String>)

    @Query("DELETE FROM PendingNostrEvent WHERE clientPubKey = :clientPubKey")
    suspend fun deleteByClientPubKey(clientPubKey: String)

    @Query("SELECT * FROM PendingNostrEvent WHERE signerPubKey = :signerPubKey")
    fun observeAllBySignerPubKey(signerPubKey: String): Flow<List<PendingNostrEvent>>
}
