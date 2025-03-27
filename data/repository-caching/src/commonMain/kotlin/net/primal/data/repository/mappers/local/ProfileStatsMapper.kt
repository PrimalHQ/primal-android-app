package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.profiles.ProfileStats as ProfileStatsPO
import net.primal.domain.model.ProfileStats as ProfileStatsDO

fun ProfileStatsPO.asProfileStatsDO(): ProfileStatsDO {
    return ProfileStatsDO(
        profileId = this.profileId,
        following = this.following,
        followers = this.followers,
        notesCount = this.notesCount,
        readsCount = this.readsCount,
        mediaCount = this.mediaCount,
        repliesCount = this.repliesCount,
        relaysCount = this.relaysCount,
        totalReceivedZaps = this.totalReceivedZaps,
        contentZapCount = this.contentZapCount,
        totalReceivedSats = this.totalReceivedSats,
        joinedAt = this.joinedAt,
    )
}
