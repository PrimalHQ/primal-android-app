package net.primal.android.core.compose.feed.model

import net.primal.android.attachments.domain.CdnImage

data class FeedPostsSyncStats(
    val latestNoteIds: List<String> = emptyList(),
    val latestAvatarCdnImages: List<CdnImage> = emptyList(),
)
