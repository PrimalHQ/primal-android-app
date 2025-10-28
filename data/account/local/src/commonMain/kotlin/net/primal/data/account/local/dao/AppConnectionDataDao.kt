package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import net.primal.shared.data.local.encryption.Encryptable

@Dao
interface AppConnectionDataDao {
    @Upsert
    suspend fun upsertAll(data: List<AppConnectionData>)

    @Transaction
    @Query("SELECT * FROM AppConnectionData WHERE signerPubKey = :signerPubKey")
    fun observeAllConnections(signerPubKey: Encryptable<String>): Flow<List<AppConnection>>

    @Transaction
    @Query("SELECT * FROM AppConnectionData WHERE signerPubKey = :signerPubKey")
    suspend fun getAll(signerPubKey: Encryptable<String>): List<AppConnection>

    @Query("DELETE FROM AppConnectionData WHERE connectionId = :connectionId")
    suspend fun deleteConnection(connectionId: String)

    @Query("DELETE FROM AppConnectionData WHERE userPubKey = :userPubKey")
    suspend fun deleteConnectionsByUser(userPubKey: Encryptable<String>)

    @Query("SELECT userPubKey FROM AppConnectionData WHERE clientPubKey = :clientPubKey")
    suspend fun getUserPubKey(clientPubKey: Encryptable<String>): String?

    @Transaction
    @Query("SELECT * FROM AppConnectionData WHERE clientPubKey = :clientPubKey")
    suspend fun getConnectionByClientPubKey(clientPubKey: Encryptable<String>): AppConnection?
}
