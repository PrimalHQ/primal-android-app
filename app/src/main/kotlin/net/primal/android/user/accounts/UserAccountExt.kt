package net.primal.android.user.accounts

import net.primal.android.user.domain.UserAccount

fun UserAccount.copyIfNotNull(
    profile: UserAccount?,
    stats: UserAccount?,
    contacts: UserAccount?,
): UserAccount =
    this.copyProfileIfNotNull(profile = profile)
        .copyStatsIfNotNull(stats = stats)
        .copyContactsIfNotNull(contacts = contacts)

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

fun UserAccount.copyContactsIfNotNull(contacts: UserAccount?): UserAccount {
    return if (contacts != null) {
        copy(
            relays = contacts.relays,
            following = contacts.following,
            followers = contacts.followers,
            interests = contacts.interests,
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
