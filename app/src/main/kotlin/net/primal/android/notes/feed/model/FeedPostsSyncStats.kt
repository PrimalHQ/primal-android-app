package net.primal.android.notes.feed.model

import net.primal.android.events.domain.CdnImage

data class FeedPostsSyncStats(
    val latestNoteIds: List<String> = emptyList(),
    val latestAvatarCdnImages: List<CdnImage?> = emptyList(),
)
