package net.primal.domain.account.service

import net.primal.core.utils.Result
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.model.LocalSignerMethodResponse
import net.primal.domain.account.model.SessionEventUserChoice

interface LocalSignerService {
    suspend fun processMethod(method: LocalSignerMethod): Result<LocalSignerMethodResponse>

    suspend fun processMethodOrAddToPending(method: LocalSignerMethod): Result<Unit>

    fun getAllMethodResponses(): List<LocalSignerMethodResponse>

    suspend fun respondToUserActions(eventChoices: List<SessionEventUserChoice>)

    suspend fun addNewApp(app: LocalApp): Result<Unit>
}
