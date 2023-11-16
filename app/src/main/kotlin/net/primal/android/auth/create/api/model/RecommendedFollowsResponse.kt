package net.primal.android.auth.create.api.model

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent

@Serializable
data class Suggestion(val name: String, val pubkey: String)

@Serializable
data class SuggestionGroup(val group: String, val members: List<Suggestion>)

@Serializable
data class RecommendedFollowsResponse(
    val metadata: Map<String, NostrEvent>,
    val suggestions: List<SuggestionGroup>,
)
