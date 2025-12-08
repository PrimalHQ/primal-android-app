package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.primal.shared.data.local.encryption.Encryptable

@Dao
interface AppConnectionDataDao {
    @Insert
    suspend fun insert(data: AppConnectionData)

    @Transaction
    @Query("SELECT * FROM AppConnectionData WHERE signerPubKey = :signerPubKey")
    fun observeAllConnections(signerPubKey: Encryptable<String>): Flow<List<AppConnection>>

    @Transaction
    @Query("SELECT * FROM AppConnectionData WHERE clientPubKey = :clientPubKey")
    fun observeConnection(clientPubKey: String): Flow<AppConnection?>

    @Transaction
    @Query("SELECT * FROM AppConnectionData WHERE signerPubKey = :signerPubKey")
    suspend fun getAll(signerPubKey: Encryptable<String>): List<AppConnection>

    @Transaction
    @Query("SELECT * FROM AppConnectionData WHERE signerPubKey = :signerPubKey AND autoStart = true")
    suspend fun getAllAutoStartConnections(signerPubKey: Encryptable<String>): List<AppConnection>

    @Query("DELETE FROM AppConnectionData WHERE clientPubKey = :clientPubKey")
    suspend fun deleteConnection(clientPubKey: String)

    @Transaction
    @Query("SELECT * FROM AppConnectionData WHERE clientPubKey = :clientPubKey")
    suspend fun getConnection(clientPubKey: String): AppConnection?

    @Query("UPDATE AppConnectionData SET name = :name WHERE clientPubKey = :clientPubKey")
    suspend fun updateConnectionName(clientPubKey: String, name: Encryptable<String>)

    @Query("UPDATE AppConnectionData SET autoStart = :autoStart WHERE clientPubKey = :clientPubKey")
    suspend fun updateConnectionAutoStart(clientPubKey: String, autoStart: Boolean)

    @Query("UPDATE AppConnectionData SET trustLevel = :trustLevel WHERE clientPubKey = :clientPubKey")
    suspend fun updateTrustLevel(clientPubKey: String, trustLevel: TrustLevel)
}
