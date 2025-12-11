package net.primal.domain.account.repository

import net.primal.core.utils.Result
import net.primal.domain.account.model.LocalApp

interface LocalConnectionRepository {
    suspend fun upsertApp(app: LocalApp): Result<Unit>
}
