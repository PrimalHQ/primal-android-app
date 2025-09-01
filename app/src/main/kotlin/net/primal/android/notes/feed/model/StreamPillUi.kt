package net.primal.android.notes.feed.model

import net.primal.domain.links.CdnImage
import net.primal.domain.streams.Stream

data class StreamPillUi(
    val naddr: String,
    val title: String?,
    val currentParticipants: Int,
    val hostProfileId: String,
    val hostAvatarCdnImage: CdnImage?,
)

fun Stream.asStreamPillUi() =
    StreamPillUi(
        naddr = this.toNaddrString(),
        title = this.title,
        currentParticipants = this.currentParticipants ?: 0,
        hostProfileId = this.mainHostId,
        hostAvatarCdnImage = this.mainHostProfile?.avatarCdnImage,
    )
