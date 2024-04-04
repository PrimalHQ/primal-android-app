package net.primal.android.user.accounts

import net.primal.android.user.domain.UserAccount

fun UserAccount.copyIfNotNull(
    profile: UserAccount?,
    stats: UserAccount?,
    followList: UserAccount?,
    bookmarksList: UserAccount?,
): UserAccount =
    this.copyProfileIfNotNull(profile = profile)
        .copyStatsIfNotNull(stats = stats)
        .copyFollowListIfNotNull(followList = followList)
        .copyBookmarksListIfNotNull(bookmarksList = bookmarksList)

fun UserAccount.copyProfileIfNotNull(profile: UserAccount?): UserAccount {
    return if (profile != null) {
        copy(
            authorDisplayName = profile.authorDisplayName,
            userDisplayName = profile.userDisplayName,
            avatarCdnImage = profile.avatarCdnImage,
            internetIdentifier = profile.internetIdentifier,
            lightningAddress = profile.lightningAddress,
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

fun UserAccount.copyBookmarksListIfNotNull(bookmarksList: UserAccount?): UserAccount {
    return if (bookmarksList != null) {
        copy(bookmarks = bookmarksList.bookmarks)
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
