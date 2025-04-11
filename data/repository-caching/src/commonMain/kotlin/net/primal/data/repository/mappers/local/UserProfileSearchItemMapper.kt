package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.profiles.ProfileStats
import net.primal.domain.common.UserProfileSearchItem

fun UserProfileSearchItem.asProfileStatsPO(): ProfileStats {
    return ProfileStats(
        profileId = this.metadata.profileId,
        followers = this.followersCount,
    )
}
