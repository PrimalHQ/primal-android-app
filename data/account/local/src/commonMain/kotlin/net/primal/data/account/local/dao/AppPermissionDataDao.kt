package net.primal.data.account.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AppPermissionDataDao {
    @Upsert
    suspend fun upsertAll(data: List<AppPermissionData>)

    @Upsert
    suspend fun upsert(data: AppPermissionData)

    @Query("DELETE FROM AppPermissionData WHERE connectionId = :connectionId")
    suspend fun deletePermissionsByConnectionId(connectionId: String)

    @Query("SELECT * FROM AppPermissionData WHERE permissionId = :permissionId AND connectionId = :connectionId")
    suspend fun findPermission(permissionId: String, connectionId: String): AppPermissionData?

    @Query("SELECT * FROM AppPermissionData WHERE connectionId = :connectionId")
    fun observePermissions(connectionId: String): Flow<List<AppPermissionData>>
}
