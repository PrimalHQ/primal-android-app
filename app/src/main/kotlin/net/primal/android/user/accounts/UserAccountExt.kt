package net.primal.android.user.accounts

import net.primal.android.user.domain.UserAccount

fun UserAccount.copyIfNotNull(profile: UserAccount?, contacts: UserAccount?): UserAccount {
    return copyProfileIfNotNull(profile = profile).copyContactsIfNotNull(contacts = contacts)
}

fun UserAccount.copyProfileIfNotNull(profile: UserAccount?): UserAccount {
    return if (profile != null) {
        copy(
            authorDisplayName = profile.authorDisplayName,
            userDisplayName = profile.userDisplayName,
            avatarCdnImage = profile.avatarCdnImage,
            internetIdentifier = profile.internetIdentifier,
            lightningAddress = profile.lightningAddress,
            followersCount = profile.followersCount,
            followingCount = profile.followingCount,
            notesCount = profile.notesCount,
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
