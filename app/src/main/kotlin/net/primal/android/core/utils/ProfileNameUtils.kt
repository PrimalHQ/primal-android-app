package net.primal.android.core.utils

import net.primal.android.nostr.model.content.ContentMetadata
import net.primal.android.profile.db.ProfileData
import net.primal.android.profile.domain.ProfileMetadata

fun ProfileData.userNameUiFriendly(): String =
    userNameUiFriendly(displayName, handle, ownerId)

fun ProfileData.authorNameUiFriendly(): String =
    authorNameUiFriendly(displayName, handle, ownerId)

fun ContentMetadata.userNameUiFriendly(pubkey: String) =
    userNameUiFriendly(displayName, name, pubkey)

fun ContentMetadata.authorNameUiFriendly(pubkey: String): String =
    authorNameUiFriendly(displayName, name, pubkey)

fun ProfileMetadata.userNameUiFriendly(userId: String): String =
    userNameUiFriendly(displayName, handle, userId)

fun ProfileMetadata.authorNameUiFriendly(userId: String): String =
    authorNameUiFriendly(displayName, handle, userId)


private fun authorNameUiFriendly(displayName: String?, name: String?, pubkey: String): String {
    return when {
        displayName?.isNotEmpty() == true -> displayName
        name?.isNotEmpty() == true -> name
        else -> pubkey.asEllipsizedNpub()
    }
}

private fun userNameUiFriendly(displayName: String?, name: String?, pubkey: String): String {
    return when {
        name?.isNotEmpty() == true -> name
        displayName?.isNotEmpty() == true -> displayName
        else -> pubkey.asEllipsizedNpub()
    }
}
