package net.primal.android.notes.feed.model

import net.primal.domain.links.CdnImage

data class FeedPostsSyncStats(
    val latestNotesCount: Int = 0,
    val latestNoteIds: List<String> = emptyList(),
    val latestAvatarCdnImages: List<CdnImage?> = emptyList(),
)
