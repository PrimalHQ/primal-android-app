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

    @Query("DELETE FROM AppPermissionData WHERE clientPubKey = :clientPubKey")
    suspend fun deletePermissions(clientPubKey: String)

    @Query("SELECT * FROM AppPermissionData WHERE permissionId = :permissionId AND clientPubKey = :clientPubKey")
    suspend fun findPermission(permissionId: String, clientPubKey: String): AppPermissionData?

    @Query("SELECT * FROM AppPermissionData WHERE clientPubKey = :clientPubKey")
    fun observePermissions(clientPubKey: String): Flow<List<AppPermissionData>>

    @Query("SELECT * FROM AppPermissionData WHERE clientPubKey = :clientPubKey")
    suspend fun findPermissionsByClientPubKey(clientPubKey: String): List<AppPermissionData>
}
