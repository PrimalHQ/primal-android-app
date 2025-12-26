package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.AppPermissionAction
import net.primal.domain.account.model.AppPermissionGroup

interface PermissionsRepository {
    suspend fun updatePermissionsAction(
        permissionIds: List<String>,
        appIdentifier: String,
        action: AppPermissionAction,
    ): Result<Unit>

    fun observePermissions(appIdentifier: String): Flow<List<AppPermissionGroup>>

    suspend fun getNamingMap(): Result<Map<String, String>>

    suspend fun resetPermissionsToDefault(identifier: String): Result<Unit>
}
