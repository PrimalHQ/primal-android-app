package net.primal.android.networking.sockets.model

import kotlinx.serialization.json.JsonObject
import net.primal.android.nostr.model.NostrVerb
import java.util.UUID

data class IncomingMessage(
    val type: NostrVerb.Incoming,
    val subscriptionId: UUID,
    val data: JsonObject? = null,
)
