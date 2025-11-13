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

    @Query("SELECT * FROM AppPermissionData WHERE permissionId = :permissionId AND connectionId = :connectionId")
    suspend fun findPermission(permissionId: String, connectionId: String): AppPermissionData?

    @Query(
        """
        UPDATE AppPermissionData
        SET `action` = :action
        WHERE permissionId = :permissionId AND connectionId = :connectionId
    """,
    )
    suspend fun updatePreference(
        permissionId: String,
        connectionId: String,
        action: PermissionAction,
    )
}
