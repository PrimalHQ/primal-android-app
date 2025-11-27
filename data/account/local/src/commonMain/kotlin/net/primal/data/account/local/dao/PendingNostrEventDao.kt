package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import net.primal.shared.data.local.encryption.Encryptable

@Dao
interface PendingNostrEventDao {
    @Upsert
    suspend fun upsertAll(data: List<PendingNostrEvent>)

    @Query("DELETE FROM PendingNostrEvent WHERE eventId IN (:eventIds)")
    suspend fun deleteByIds(eventIds: List<String>)

    @Query("SELECT * FROM PendingNostrEvent WHERE signerPubKey = :signerPubKey")
    fun observeAllBySignerPubKey(signerPubKey: Encryptable<String>): Flow<List<PendingNostrEvent>>
}
