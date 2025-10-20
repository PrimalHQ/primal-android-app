package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface AppConnectionDataDao {
    @Upsert
    fun upsertAll(data: List<AppConnectionData>)

    @Query("SELECT * FROM AppConnectionData")
    fun getAll(): List<AppConnection>

    @Query("DELETE FROM AppConnectionData WHERE connectionId = :connectionId")
    fun deleteConnection(connectionId: String)

    @Query("DELETE FROM AppConnectionData WHERE userPubKey = :userPubKey")
    fun deleteConnectionsByUser(userPubKey: String)

    @Query("SELECT userPubKey FROM AppConnectionData WHERE clientPubKey = :clientPubKey")
    fun getUserPubKey(clientPubKey: String): String?
}
