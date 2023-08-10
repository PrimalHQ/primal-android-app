package net.primal.android.user.accounts

import net.primal.android.user.domain.UserAccount

fun UserAccount.merge(profile: UserAccount?, contacts: UserAccount?) = this.copy(
    authorDisplayName = profile?.authorDisplayName ?: contacts?.authorDisplayName ?: this.authorDisplayName,
    userDisplayName = profile?.userDisplayName ?: contacts?.userDisplayName ?: this.userDisplayName,
    pictureUrl = profile?.pictureUrl,
    internetIdentifier = profile?.internetIdentifier,
    followersCount = profile?.followersCount,
    followingCount = profile?.followingCount,
    notesCount = profile?.notesCount,
    relays = contacts?.relays ?: emptyList(),
    following = contacts?.following ?: emptySet(),
    followers = contacts?.followers ?: emptyList(),
    interests = contacts?.interests ?: emptyList(),
)
