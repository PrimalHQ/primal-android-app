@file:Suppress("TooManyFunctions")

package net.primal.android.core.utils

import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.user.domain.UserAccount
import net.primal.domain.nostr.ContentMetadata
import net.primal.domain.nostr.utils.asEllipsizedNpub
import net.primal.domain.profile.ProfileData as ProfileDataDO

fun ProfileDataDO.usernameUiFriendly(): String = usernameUiFriendly(this.displayName, this.handle, this.profileId)

fun ProfileDataDO.authorNameUiFriendly(): String = authorNameUiFriendly(this.displayName, this.handle, this.profileId)

fun UserAccount.authorNameUiFriendly(): String =
    authorNameUiFriendly(this.authorDisplayName, this.userDisplayName, this.pubkey)

fun ContentMetadata.usernameUiFriendly(pubkey: String) =
    usernameUiFriendly(
        displayName,
        name,
        pubkey,
    )

fun ContentMetadata.authorNameUiFriendly(pubkey: String): String =
    authorNameUiFriendly(
        displayName,
        name,
        pubkey,
    )

fun ProfileMetadata.usernameUiFriendly(userId: String): String =
    usernameUiFriendly(
        displayName,
        username,
        userId,
    )

fun ProfileMetadata.authorNameUiFriendly(userId: String): String =
    authorNameUiFriendly(
        displayName,
        username,
        userId,
    )

private fun authorNameUiFriendly(
    displayName: String?,
    name: String?,
    pubkey: String,
): String {
    return when {
        displayName?.isNotBlank() == true -> displayName
        name?.isNotBlank() == true -> name
        else -> pubkey.asEllipsizedNpub()
    }
}

private fun usernameUiFriendly(
    displayName: String?,
    name: String?,
    pubkey: String,
): String {
    return when {
        name?.isNotBlank() == true -> name
        displayName?.isNotBlank() == true -> displayName
        else -> pubkey.asEllipsizedNpub()
    }
}
