package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.AppPermissionAction
import net.primal.domain.account.model.AppPermissionGroup

interface PermissionsRepository {
    suspend fun updatePermissionsAction(
        permissionIds: List<String>,
        clientPubKey: String,
        action: AppPermissionAction,
    ): Result<Unit>

    fun observePermissions(clientPubKey: String): Flow<List<AppPermissionGroup>>

    suspend fun getNamingMap(): Result<Map<String, String>>

    suspend fun resetPermissionsToDefault(clientPubKey: String): Result<Unit>
}
