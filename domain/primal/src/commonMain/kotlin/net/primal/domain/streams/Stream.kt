package net.primal.domain.streams

import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.NostrEventKind

data class Stream(
    val aTag: String,
    val authorId: String,
    val dTag: String,
    val title: String?,
    val summary: String?,
    val imageUrl: String?,
    val hashtags: List<String>,
    val streamingUrl: String?,
    val recordingUrl: String?,
    val startsAt: Long?,
    val endsAt: Long?,
    val status: StreamStatus,
    val currentParticipants: Int?,
    val totalParticipants: Int?,
) {
    fun isLive() = status == StreamStatus.LIVE

    fun toNaddrString(): String? =
        Naddr(
            kind = NostrEventKind.LiveActivity.value,
            userId = this.authorId,
            identifier = this.dTag,
        ).toNaddrString()
}
