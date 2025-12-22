package net.primal.data.account.local.dao.apps.remote

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.data.account.local.dao.apps.TrustLevel
import net.primal.shared.data.local.encryption.Encryptable

@Dao
interface RemoteAppConnectionDataDao {
    @Insert
    suspend fun insert(data: RemoteAppConnectionData)

    @Transaction
    @Query("SELECT * FROM RemoteAppConnectionData WHERE signerPubKey = :signerPubKey")
    fun observeAllConnections(signerPubKey: String): Flow<List<RemoteAppConnection>>

    @Transaction
    @Query("SELECT * FROM RemoteAppConnectionData WHERE clientPubKey = :clientPubKey")
    fun observeConnection(clientPubKey: String): Flow<RemoteAppConnection?>

    @Transaction
    @Query("SELECT * FROM RemoteAppConnectionData WHERE signerPubKey = :signerPubKey")
    suspend fun getAll(signerPubKey: String): List<RemoteAppConnection>

    @Transaction
    @Query("SELECT * FROM RemoteAppConnectionData WHERE userPubKey = :userPubKey")
    suspend fun getConnectionsByUser(userPubKey: String): List<RemoteAppConnection>

    @Transaction
    @Query("SELECT * FROM RemoteAppConnectionData WHERE signerPubKey = :signerPubKey AND autoStart = 1")
    suspend fun getAllAutoStartConnections(signerPubKey: String): List<RemoteAppConnection>

    @Query("DELETE FROM RemoteAppConnectionData WHERE clientPubKey = :clientPubKey")
    suspend fun deleteConnection(clientPubKey: String)

    @Transaction
    @Query("SELECT * FROM RemoteAppConnectionData WHERE clientPubKey = :clientPubKey")
    suspend fun getConnection(clientPubKey: String): RemoteAppConnection?

    @Query("UPDATE RemoteAppConnectionData SET name = :name WHERE clientPubKey = :clientPubKey")
    suspend fun updateConnectionName(clientPubKey: String, name: Encryptable<String>)

    @Query("UPDATE RemoteAppConnectionData SET autoStart = :autoStart WHERE clientPubKey = :clientPubKey")
    suspend fun updateConnectionAutoStart(clientPubKey: String, autoStart: Boolean)

    @Query("UPDATE RemoteAppConnectionData SET trustLevel = :trustLevel WHERE clientPubKey = :clientPubKey")
    suspend fun updateTrustLevel(clientPubKey: String, trustLevel: TrustLevel)
}
