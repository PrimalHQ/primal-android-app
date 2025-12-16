package net.primal.domain.account.service

import net.primal.core.utils.Result
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.model.LocalSignerMethodResponse

interface LocalSignerService {
    suspend fun processMethod(method: LocalSignerMethod): Result<LocalSignerMethodResponse>

    suspend fun addNewApp(app: LocalApp): Result<Unit>
}
