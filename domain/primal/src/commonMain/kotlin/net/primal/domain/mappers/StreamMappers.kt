package net.primal.domain.mappers

import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.links.ReferencedStream
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.findAllHashtags
import net.primal.domain.nostr.findFirstATag
import net.primal.domain.nostr.findFirstCurrentParticipants
import net.primal.domain.nostr.findFirstEnds
import net.primal.domain.nostr.findFirstHostPubkey
import net.primal.domain.nostr.findFirstIdentifier
import net.primal.domain.nostr.findFirstImage
import net.primal.domain.nostr.findFirstRecording
import net.primal.domain.nostr.findFirstStarts
import net.primal.domain.nostr.findFirstStatus
import net.primal.domain.nostr.findFirstStreaming
import net.primal.domain.nostr.findFirstSummary
import net.primal.domain.nostr.findFirstTitle
import net.primal.domain.nostr.findFirstTotalParticipants
import net.primal.domain.nostr.utils.authorNameUiFriendly
import net.primal.domain.profile.ProfileData
import net.primal.domain.streams.Stream
import net.primal.domain.streams.StreamStatus

fun Stream.asReferencedStream() =
    ReferencedStream(
        naddr = this.toNaddrString(),
        title = this.title,
        currentParticipants = this.currentParticipants,
        totalParticipants = this.totalParticipants,
        startedAt = this.startsAt,
        endedAt = this.endsAt,
        status = this.status,
        mainHostId = this.mainHostId,
        mainHostName = authorNameUiFriendly(
            displayName = this.mainHostProfile?.displayName,
            name = this.mainHostProfile?.handle,
            pubkey = this.mainHostId,
        ),
        mainHostIsLive = this.isLive(),
        mainHostAvatarCdnImage = this.mainHostProfile?.avatarCdnImage,
        mainHostLegendProfile = this.mainHostProfile?.primalPremiumInfo?.legendProfile,
        mainHostInternetIdentifier = this.mainHostProfile?.internetIdentifier,
    )

fun List<NostrEvent>.mapAsStreamDO(profilesMap: Map<String, ProfileData>) =
    mapNotNull { event ->
        val authorId = event.tags.findFirstHostPubkey() ?: event.pubKey
        val dTag = event.tags.findFirstIdentifier() ?: return@mapNotNull null
        Stream(
            aTag = event.tags.findFirstATag() ?: "${NostrEventKind.LiveActivity.value}:${event.pubKey}:$dTag",
            eventId = event.id,
            authorId = authorId,
            authorProfile = profilesMap[authorId],
            dTag = dTag,
            title = event.tags.findFirstTitle(),
            summary = event.tags.findFirstSummary(),
            imageUrl = event.tags.findFirstImage(),
            hashtags = event.tags.findAllHashtags(),
            streamingUrl = event.tags.findFirstStreaming(),
            recordingUrl = event.tags.findFirstRecording(),
            startsAt = event.tags.findFirstStarts()?.toLong(),
            endsAt = event.tags.findFirstEnds()?.toLong(),
            status = StreamStatus.fromString(event.tags.findFirstStatus()),
            currentParticipants = event.tags.findFirstCurrentParticipants()?.toInt(),
            totalParticipants = event.tags.findFirstTotalParticipants()?.toInt(),
            eventZaps = emptyList(),
            rawNostrEventJson = event.encodeToJsonString(),
        )
    }
