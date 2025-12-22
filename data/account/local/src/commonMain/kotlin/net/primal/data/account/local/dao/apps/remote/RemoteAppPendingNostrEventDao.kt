package net.primal.data.account.local.dao.apps.remote

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RemoteAppPendingNostrEventDao {
    @Upsert
    suspend fun upsertAll(data: List<RemoteAppPendingNostrEvent>)

    @Query("DELETE FROM RemoteAppPendingNostrEvent WHERE eventId IN (:eventIds)")
    suspend fun deleteByIds(eventIds: List<String>)

    @Query("DELETE FROM RemoteAppPendingNostrEvent WHERE clientPubKey = :clientPubKey")
    suspend fun deleteByClientPubKey(clientPubKey: String)

    @Query("SELECT * FROM RemoteAppPendingNostrEvent WHERE signerPubKey = :signerPubKey")
    fun observeAllBySignerPubKey(signerPubKey: String): Flow<List<RemoteAppPendingNostrEvent>>
}
