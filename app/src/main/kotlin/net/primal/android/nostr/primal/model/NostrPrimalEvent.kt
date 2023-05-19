package net.primal.android.nostr.primal.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEventKind

@Serializable
data class NostrPrimalEvent(
    val kind: NostrEventKind,
    val content: String,
)
