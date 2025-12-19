package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.model.PermissionAction

interface LocalAppRepository {
    suspend fun upsertApp(app: LocalApp): Result<Unit>

    suspend fun getPermissionActionForMethod(method: LocalSignerMethod): PermissionAction

    fun observeAllApps(): Flow<List<LocalApp>>
}
