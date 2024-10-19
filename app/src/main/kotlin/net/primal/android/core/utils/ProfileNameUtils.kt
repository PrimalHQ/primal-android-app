package net.primal.android.core.utils

import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.profile.db.ProfileData
import net.primal.android.profile.domain.ProfileMetadata
import net.primal.android.stats.db.EventZap

fun ProfileData.usernameUiFriendly(): String = usernameUiFriendly(this.displayName, this.handle, this.ownerId)

fun ProfileData.authorNameUiFriendly(): String = authorNameUiFriendly(this.displayName, this.handle, this.ownerId)

fun EventZap.usernameUiFriendly(): String =
    usernameUiFriendly(this.zapSenderDisplayName, this.zapSenderHandle, this.zapSenderId)

fun EventZap.authorNameUiFriendly(): String =
    authorNameUiFriendly(this.zapSenderDisplayName, this.zapSenderHandle, this.zapSenderId)

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
