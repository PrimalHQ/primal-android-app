package net.primal.android.notes.feed.model

import net.primal.domain.links.CdnImage

data class StreamsSyncStats(
    val streamsCount: Int = 0,
    val streamAvatarCdnImages: List<CdnImage?> = emptyList(),
)
