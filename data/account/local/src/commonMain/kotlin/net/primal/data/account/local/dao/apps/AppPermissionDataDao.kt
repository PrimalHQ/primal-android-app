package net.primal.data.account.local.dao.apps

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

    @Query("DELETE FROM AppPermissionData WHERE appIdentifier = :appIdentifier")
    suspend fun deletePermissions(appIdentifier: String)

    @Query("UPDATE AppPermissionData SET `action` = :action WHERE appIdentifier = :appIdentifier")
    suspend fun updateActionByAppIdentifier(appIdentifier: String, action: PermissionAction)

    @Query("SELECT * FROM AppPermissionData WHERE permissionId = :permissionId AND appIdentifier = :appIdentifier")
    suspend fun findPermission(permissionId: String, appIdentifier: String): AppPermissionData?

    @Query("SELECT * FROM AppPermissionData WHERE appIdentifier = :appIdentifier")
    fun observePermissions(appIdentifier: String): Flow<List<AppPermissionData>>

    @Query("SELECT * FROM AppPermissionData WHERE appIdentifier = :appIdentifier")
    suspend fun findPermissionsByAppIdentifier(appIdentifier: String): List<AppPermissionData>
}
