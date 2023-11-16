package net.primal.android.core.utils

import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.profile.db.ProfileData
import net.primal.android.profile.domain.ProfileMetadata

fun ProfileData.usernameUiFriendly(): String = usernameUiFriendly(displayName, handle, ownerId)

fun ProfileData.authorNameUiFriendly(): String = authorNameUiFriendly(displayName, handle, ownerId)

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
