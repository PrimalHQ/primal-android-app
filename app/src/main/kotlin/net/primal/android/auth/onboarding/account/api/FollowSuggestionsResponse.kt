package net.primal.android.auth.onboarding.account.api

import kotlinx.serialization.Serializable
import net.primal.android.nostr.model.NostrEvent

@Serializable
data class FollowSuggestionsResponse(
    val metadata: Map<String, NostrEvent>,
    val suggestions: List<Suggestion>,
)

@Serializable
data class Suggestion(val group: String, val members: List<SuggestionMember>)

@Serializable
data class SuggestionMember(val name: String, val pubkey: String)
