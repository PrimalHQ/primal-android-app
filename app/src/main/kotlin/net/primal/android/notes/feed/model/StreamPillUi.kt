package net.primal.android.notes.feed.model

import net.primal.domain.links.CdnImage
import net.primal.domain.streams.Stream

data class StreamPillUi(
    val naddr: String,
    val currentParticipants: Int?,
    val title: String?,
    val hostProfileId: String,
    val hostAvatarCdnImage: CdnImage?,
)

fun Stream.asStreamPillUi() =
    StreamPillUi(
        naddr = this.toNaddrString(),
        currentParticipants = this.currentParticipants,
        title = this.title,
        hostProfileId = this.mainHostId,
        hostAvatarCdnImage = this.mainHostProfile?.avatarCdnImage,
    )
