package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface AppPermissionDataDao {
    @Upsert
    suspend fun upsertAll(data: List<AppPermissionData>)

    @Query("DELETE FROM AppPermissionData WHERE connectionId = :connectionId")
    suspend fun deletePermissionsByConnectionId(connectionId: String)
}
