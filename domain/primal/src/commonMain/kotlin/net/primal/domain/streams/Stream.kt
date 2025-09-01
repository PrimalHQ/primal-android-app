package net.primal.domain.streams

import net.primal.domain.events.EventZap
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.profile.ProfileData
import net.primal.domain.streams.mappers.resolveStreamStatus

data class Stream(
    val aTag: String,
    val eventId: String,
    val eventAuthorId: String,
    val mainHostId: String,
    val mainHostProfile: ProfileData?,
    val dTag: String,
    val title: String?,
    val summary: String?,
    val imageUrl: String?,
    val hashtags: List<String>,
    val streamingUrl: String?,
    val recordingUrl: String?,
    val startsAt: Long?,
    val endsAt: Long?,
    val currentParticipants: Int?,
    val totalParticipants: Int?,
    val eventZaps: List<EventZap> = emptyList(),
    val rawNostrEventJson: String,
    val createdAt: Long,
    internal val status: StreamStatus,
) {
    val resolvedStatus
        get() = resolveStreamStatus(
            status = status,
            streamingUrl = streamingUrl,
            startsAt = startsAt,
            endsAt = endsAt,
            createdAt = createdAt,
        )

    fun isLive() = resolvedStatus == StreamStatus.LIVE

    fun toNaddrString(): String =
        Naddr(
            kind = NostrEventKind.LiveActivity.value,
            userId = this.eventAuthorId,
            identifier = this.dTag,
        ).toNaddrString()
}
