package net.primal.domain

import kotlinx.datetime.Instant
import net.primal.domain.model.FeedPost
import net.primal.domain.model.ProfileData

data class ExploreZapNoteData(
    val sender: ProfileData?,
    val receiver: ProfileData?,
    val noteData: FeedPost,
    val amountSats: ULong,
    val zapMessage: String?,
    val createdAt: Instant,
    val noteNostrUris: List<EventUriNostrReference> = emptyList(),
)
