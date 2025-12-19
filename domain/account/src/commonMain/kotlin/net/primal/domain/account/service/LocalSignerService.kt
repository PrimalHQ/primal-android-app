package net.primal.domain.account.service

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.model.LocalSignerMethodResponse
import net.primal.domain.account.model.SessionEventUserChoice

interface LocalSignerService {
    suspend fun processMethod(method: LocalSignerMethod): Result<LocalSignerMethodResponse>

    fun subscribeToPendingUserActions(): Flow<List<LocalSignerMethod>>

    fun getMethodResponses(): List<LocalSignerMethodResponse>

    suspend fun respondToUserActions(eventChoices: List<SessionEventUserChoice>)

    suspend fun addNewApp(app: LocalApp): Result<Unit>

    class InsufficientPermissions : RuntimeException("Insufficient permissions to execute this method.")
    class UserAutoRejected : RuntimeException("User auto rejected this method.")
}
