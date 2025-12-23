package net.primal.domain.account.repository

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.AppPermissionAction
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.model.TrustLevel

interface LocalAppRepository {
    suspend fun upsertApp(app: LocalApp): Result<Unit>

    suspend fun getPermissionActionForMethod(method: LocalSignerMethod): AppPermissionAction

    fun observeAllApps(): Flow<List<LocalApp>>

    fun observeApp(identifier: String): Flow<LocalApp?>

    suspend fun updateTrustLevel(identifier: String, trustLevel: TrustLevel): Result<Unit>

    suspend fun deleteApp(identifier: String): Result<Unit>
}
