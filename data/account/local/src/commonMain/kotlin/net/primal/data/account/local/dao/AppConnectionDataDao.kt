package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import net.primal.shared.data.local.encryption.Encryptable

@Dao
interface AppConnectionDataDao {
    @Upsert
    fun upsertAll(data: List<AppConnectionData>)

    @Transaction
    @Query("SELECT * FROM AppConnectionData WHERE signerPubKey = :signerPubKey")
    fun getAll(signerPubKey: Encryptable<String>): List<AppConnection>

    @Query("DELETE FROM AppConnectionData WHERE connectionId = :connectionId")
    fun deleteConnection(connectionId: String)

    @Query("DELETE FROM AppConnectionData WHERE userPubKey = :userPubKey")
    fun deleteConnectionsByUser(userPubKey: Encryptable<String>)

    @Query("SELECT userPubKey FROM AppConnectionData WHERE clientPubKey = :clientPubKey")
    fun getUserPubKey(clientPubKey: Encryptable<String>): String?

    @Transaction
    @Query("SELECT * FROM AppConnectionData WHERE clientPubKey = :clientPubKey")
    fun getConnectionByClientPubKey(clientPubKey: Encryptable<String>): AppConnection?
}
