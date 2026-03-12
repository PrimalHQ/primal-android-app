package net.primal.android.auth.onboarding.account.api

import net.primal.android.auth.onboarding.account.ui.model.FollowPackMember
import net.primal.android.auth.onboarding.account.ui.model.OnboardingFollowPack
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.nostr.ContentMetadata

fun FollowSuggestionsResponse.asFollowPacks(): List<OnboardingFollowPack> {
    return suggestions.map { suggestion ->
        OnboardingFollowPack(
            name = suggestion.name,
            coverUrl = suggestion.coverUrl?.ifBlank { null },
            members = suggestion.people.map { member ->
                val metadata = metadata[member.userId]?.content
                    .decodeFromJsonStringOrNull<ContentMetadata>()
                FollowPackMember(
                    userId = member.userId,
                    displayName = metadata?.displayName
                        ?: metadata?.name
                        ?: member.name,
                    about = metadata?.about,
                    avatarUrl = metadata?.picture,
                )
            },
        )
    }
}
