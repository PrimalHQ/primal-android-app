package net.primal.data.remote.api.events.model

import kotlinx.serialization.Serializable
import net.primal.domain.nostr.Naddr

@Serializable
data class ReplaceableEventRequest(
    val pubkey: String,
    val identifier: String,
    val kind: Int,
)

fun Naddr.toReplaceableEventRequest() =
    ReplaceableEventRequest(
        pubkey = this.userId,
        kind = this.kind,
        identifier = this.identifier,
    )
