package net.primal.android.user.accounts

import net.primal.android.user.domain.UserAccount

fun UserAccount.copyIfNotNull(
    profile: UserAccount?,
    stats: UserAccount?,
    followList: UserAccount?,
): UserAccount =
    this.copyProfileIfNotNull(profile = profile)
        .copyStatsIfNotNull(stats = stats)
        .copyFollowListIfNotNull(followList = followList)

fun UserAccount.copyProfileIfNotNull(profile: UserAccount?): UserAccount {
    return if (profile != null) {
        copy(
            authorDisplayName = profile.authorDisplayName,
            userDisplayName = profile.userDisplayName,
            avatarCdnImage = profile.avatarCdnImage,
            internetIdentifier = profile.internetIdentifier,
            lightningAddress = profile.lightningAddress,
            avatarRing = profile.avatarRing,
            customBadge = profile.customBadge,
            legendaryStyle = profile.legendaryStyle,
        )
    } else {
        this
    }
}

fun UserAccount.copyFollowListIfNotNull(followList: UserAccount?): UserAccount {
    return if (followList != null) {
        copy(
            following = followList.following,
            interests = followList.interests,
            followListEventContent = followList.followListEventContent,
        )
    } else {
        this
    }
}

fun UserAccount.copyStatsIfNotNull(stats: UserAccount?): UserAccount {
    return if (stats != null) {
        copy(
            followersCount = stats.followersCount,
            followingCount = stats.followingCount,
            notesCount = stats.notesCount,
        )
    } else {
        this
    }
}
