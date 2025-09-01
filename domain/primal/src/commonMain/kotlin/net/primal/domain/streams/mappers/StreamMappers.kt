package net.primal.domain.streams.mappers

import net.primal.domain.links.ReferencedStream
import net.primal.domain.nostr.utils.authorNameUiFriendly
import net.primal.domain.streams.Stream

fun Stream.asReferencedStream() =
    ReferencedStream(
        naddr = this.toNaddrString(),
        title = this.title,
        currentParticipants = this.currentParticipants,
        totalParticipants = this.totalParticipants,
        startedAt = this.startsAt,
        endedAt = this.endsAt,
        status = this.resolvedStatus,
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
