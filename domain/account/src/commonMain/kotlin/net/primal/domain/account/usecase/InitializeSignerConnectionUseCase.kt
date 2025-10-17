package net.primal.domain.account.usecase

import net.primal.core.utils.Result

class InitializeSignerConnectionUseCase {
    /**
     * Use case should perform following actions in order, failure of one fails the process:
     * - Parse `connectionUrl` and retrieve necessary data.
     * - Send `connect` response event to relays from the `connectionUrl`.
     * - Save the data to the db.
     */
    fun invoke(connectionUrl: String): Result<Unit> {
        TODO()
    }
}
