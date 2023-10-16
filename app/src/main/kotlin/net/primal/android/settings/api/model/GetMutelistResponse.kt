package net.primal.android.settings.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent

@Serializable
data class GetMutelistResponse (
    val mutelist: NostrEvent? = null
)