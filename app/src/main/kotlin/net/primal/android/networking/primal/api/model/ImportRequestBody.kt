package net.primal.android.networking.primal.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent

@Serializable
data class ImportRequestBody(
    val events: List<NostrEvent>
)
