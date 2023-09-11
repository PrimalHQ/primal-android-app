package net.primal.android.auth.create.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEventKind

@Serializable
data class Suggestion(val name: String, val pubkey: String)

@Serializable
data class SuggestionGroup(val group: String, val members: List<Suggestion>)

@Serializable
data class Metadata(
    val content: String,
    @SerialName("created_at")
    val createdAt: Int,
    val id: String,
    val kind: NostrEventKind,
    val pubkey: String,
    val sig: String
)

@Serializable
data class RecommendedFollowsResponse(
    val metadata: Map<String, Metadata>,
    val suggestions: List<SuggestionGroup>
)
