package net.primal.domain.mappers

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
        status = this.status,
        mainHostId = this.authorId,
        mainHostName = authorNameUiFriendly(
            displayName = this.authorProfile?.displayName,
            name = this.authorProfile?.handle,
            pubkey = this.authorId,
        ),
        mainHostIsLive = this.isLive(),
        mainHostAvatarCdnImage = this.authorProfile?.avatarCdnImage,
        mainHostLegendProfile = this.authorProfile?.primalPremiumInfo?.legendProfile,
        mainHostInternetIdentifier = this.authorProfile?.internetIdentifier,
    )
