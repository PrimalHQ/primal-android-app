package net.primal.data.account.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import net.primal.core.networking.sockets.NostrSocketClientFactory
import net.primal.core.utils.Result
import net.primal.core.utils.runCatching
import net.primal.data.account.remote.command.model.NostrCommand
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.serialization.toNostrJsonObject

class RemoteSignerClient(
    relayUrl: String,
) {
    val nostrSocketClient = NostrSocketClientFactory.create(wssUrl = relayUrl)
    val incomingCommands: Flow<NostrCommand> = emptyFlow()

    init {
        /**
         * start observing `NostrEvent`s through `nostrSocketClient` and parse them to `NostrCommand`
         */
    }

    suspend fun close() {
        nostrSocketClient.close()
    }

    suspend fun publishEvent(nostrEvent: NostrEvent): Result<Unit> =
        runCatching {
            nostrSocketClient.sendEVENT(signedEvent = nostrEvent.toNostrJsonObject())
        }
}
