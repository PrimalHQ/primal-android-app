package net.primal.android.notes.feed.model

import net.primal.domain.links.CdnImage

data class StreamPillUi(
    val naddr: String,
    val currentParticipants: Int?,
    val title: String?,
    val hostProfileId: String,
    val hostAvatarCdnImage: CdnImage?,
)
