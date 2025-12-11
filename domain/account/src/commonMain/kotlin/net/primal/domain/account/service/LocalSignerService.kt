package net.primal.domain.account.service

import kotlinx.coroutines.flow.Flow
import net.primal.core.utils.Result
import net.primal.domain.account.model.LocalApp
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.model.LocalSignerMethodResponse

interface LocalSignerService {
    /* TODO(marko): rethink this approach.
     *  We are going to get x methods,
     *  some of them could be auto-sign, some auto-reject, some seeking approval.
     *  We should probably have a way of saying these are approved, these are rejected, these are waiting for approval.
     */
    // TODO processMethod()
    suspend fun respondToMethods(methods: List<LocalSignerMethod>): List<Result<LocalSignerMethodResponse>>

    fun observePendingActions(): Flow<String> // ovo cemo da osluskujemo da znamo sta treba da pitamo user da approve

    fun getResults(): List<String>
    /**
     * TODO: Should we add new app connection here or from
     *      `SignerConnectionInitializer` or perhaps create new initializer?
     *      It's worth noting that this initialize logic is a lot simpler as we are not parsing anything here.
     */
    suspend fun addNewApp(app: LocalApp): Result<Unit>
}
