package net.primal.domain.explore

import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import net.primal.domain.links.EventUriNostrReference
import net.primal.domain.posts.FeedPost
import net.primal.domain.profile.ProfileData

@OptIn(ExperimentalTime::class)
data class ExploreZapNoteData(
    val sender: ProfileData?,
    val receiver: ProfileData?,
    val noteData: FeedPost,
    val amountSats: ULong,
    val zapMessage: String?,
    val createdAt: Instant,
    val noteNostrUris: List<EventUriNostrReference> = emptyList(),
)
