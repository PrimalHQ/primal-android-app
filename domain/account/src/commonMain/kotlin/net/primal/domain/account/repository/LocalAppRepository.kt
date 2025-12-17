package net.primal.domain.account.repository

import net.primal.core.utils.Result
import net.primal.domain.account.model.LocalApp

interface LocalAppRepository {
    suspend fun upsertApp(app: LocalApp): Result<Unit>

    suspend fun canProcessMethod(permissionId: String, packageName: String): Boolean
}
