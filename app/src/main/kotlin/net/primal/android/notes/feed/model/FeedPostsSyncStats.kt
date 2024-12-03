package net.primal.android.notes.feed.model

import net.primal.android.attachments.domain.CdnImage
import net.primal.android.premium.legend.LegendaryCustomization

data class FeedPostsSyncStats(
    val latestNoteIds: List<String> = emptyList(),
    val latestAvatarCdnImages: List<CdnImage> = emptyList(),
    val latestLegendaryCustomizations: List<LegendaryCustomization?> = emptyList(),
)
