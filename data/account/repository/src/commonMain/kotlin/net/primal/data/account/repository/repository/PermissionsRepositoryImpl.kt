package net.primal.data.account.repository.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.core.utils.Result
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.map
import net.primal.core.utils.runCatching
import net.primal.data.account.local.dao.AppPermissionData
import net.primal.data.account.local.dao.PermissionAction as PermissionActionPO
import net.primal.data.account.local.db.AccountDatabase
import net.primal.data.account.remote.api.WellKnownApi
import net.primal.data.account.remote.api.model.PermissionsResponse
import net.primal.data.account.repository.mappers.asDomain
import net.primal.data.account.repository.mappers.asPO
import net.primal.domain.account.model.AppPermission
import net.primal.domain.account.model.AppPermissionGroup
import net.primal.domain.account.model.PermissionAction
import net.primal.domain.account.repository.PermissionsRepository
import net.primal.shared.data.local.db.withTransaction

class PermissionsRepositoryImpl(
    private val database: AccountDatabase,
    private val dispatchers: DispatcherProvider,
    private val wellKnownApi: WellKnownApi,
) : PermissionsRepository {
    override suspend fun updatePermissionsAction(
        permissionIds: List<String>,
        connectionId: String,
        action: PermissionAction,
    ): Result<Unit> =
        withContext(dispatchers.io()) {
            runCatching {
                database.permissions().upsertAll(
                    data = permissionIds.map {
                        AppPermissionData(
                            permissionId = it,
                            connectionId = connectionId,
                            action = action.asPO(),
                        )
                    },
                )
            }
        }

    override suspend fun observePermissions(connectionId: String): Result<Flow<List<AppPermissionGroup>>> =
        withContext(dispatchers.io()) {
            runCatching {
                val signerPermissions = wellKnownApi.getSignerPermissions()

                database.permissions().observePermissions(connectionId = connectionId)
                    .map { permissions ->
                        buildPermissionGroups(
                            response = signerPermissions,
                            permissions = permissions.map { it.asDomain() },
                        )
                    }
            }
        }

    override suspend fun getNamingMap(): Result<Map<String, String>> =
        withContext(dispatchers.io()) {
            runCatching {
                wellKnownApi.getSignerPermissions()
            }.map { (permissions, groups) ->
                groups.map { group -> group.permissionIds.associateWith { group.title } }
                    .reduce { acc, map -> acc + map }
                    .run { permissions.associate { it.id to it.title } + this }
            }
        }

    override suspend fun resetPermissionsToDefault(connectionId: String): Result<Unit> =
        withContext(dispatchers.io()) {
            runCatching {
                val mediumTrustPermissions = wellKnownApi.getMediumTrustPermissions().allowPermissions

                database.withTransaction {
                    database.permissions().deletePermissionsByConnectionId(connectionId)
                    database.permissions().upsertAll(
                        data = mediumTrustPermissions.map { permissionId ->
                            AppPermissionData(
                                permissionId = permissionId,
                                connectionId = connectionId,
                                action = PermissionActionPO.Approve,
                            )
                        },
                    )
                }
            }
        }

    private fun buildPermissionGroups(
        response: PermissionsResponse,
        permissions: List<AppPermission>,
    ): List<AppPermissionGroup> {
        val permissionsMap = permissions.associateBy { it.permissionId }
        val permissionsInGroups = response.groups.flatMap { it.permissionIds }.toHashSet()
        val singlePermissionGroups = response.permissions.filterNot { it.id in permissionsInGroups }

        return response.groups.map { group ->
            val action = group.permissionIds
                .firstOrNull { permissionsMap[it] != null }
                ?.let { permissionsMap[it]?.action }
                ?: PermissionAction.Ask
            AppPermissionGroup(
                groupId = group.id,
                title = group.title,
                action = action,
                permissionIds = group.permissionIds,
            )
        } + singlePermissionGroups.map { permission ->
            val action = permissionsMap[permission.id]?.action ?: PermissionAction.Ask

            AppPermissionGroup(
                groupId = permission.id,
                title = permission.title,
                action = action,
                permissionIds = listOf(permission.id),
            )
        }
    }
}
