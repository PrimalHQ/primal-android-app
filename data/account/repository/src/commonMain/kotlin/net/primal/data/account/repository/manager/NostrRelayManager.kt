package net.primal.data.account.repository.manager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import net.primal.core.utils.Result
import net.primal.data.account.remote.command.model.NostrCommand
import net.primal.domain.nostr.NostrEvent

internal class NostrRelayManager {
    val incomingCommands: Flow<NostrCommand> = emptyFlow()

    /**
     * Manages multiple `NostrSocketClient`s to observe relays.
     */
    fun connectToRelays(relays: List<String>) {
        TODO()
    }

    /**
     * Publishes given `nostrEvent` to multiple relays through managed `NostrSocketClient`s.
     * First successful publish should return `Result.success`. If all publishes fail, we will get `Result.failure`.
     */
    suspend fun publishEvent(relays: List<String>, nostrEvent: NostrEvent): Result<Unit> {
        TODO()
    }
}
